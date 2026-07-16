"""
SmartHire Google Calendar Integration
Creates interview events on Google Calendar and sends invites to attendees.
"""
import os
import logging
from datetime import timedelta

logger = logging.getLogger(__name__)

try:
    from google.oauth2 import service_account
    from googleapiclient.discovery import build
    GOOGLE_CALENDAR_AVAILABLE = True
except ImportError:
    GOOGLE_CALENDAR_AVAILABLE = False
    logger.warning('google-api-python-client not installed. Calendar integration disabled.')

SCOPES = ['https://www.googleapis.com/auth/calendar']


def _get_calendar_service():
    """Build and return an authenticated Google Calendar service."""
    if not GOOGLE_CALENDAR_AVAILABLE:
        return None
    # Use a dedicated calendar service account path if set, fall back to Firebase SA path
    service_account_path = os.environ.get('FIREBASE_SERVICE_ACCOUNT_PATH', '')
    cal_path = os.environ.get('GOOGLE_CALENDAR_SERVICE_ACCOUNT_PATH', service_account_path)
    if not cal_path or not os.path.exists(cal_path):
        logger.warning('Google Calendar service account not configured.')
        return None
    try:
        creds = service_account.Credentials.from_service_account_file(cal_path, scopes=SCOPES)
        return build('calendar', 'v3', credentials=creds)
    except Exception as e:
        logger.error(f'Google Calendar service init failed: {e}')
        return None


def create_calendar_event(interview, zoom_join_url: str = '') -> str:
    """
    Create a Google Calendar event for an interview.
    Sends email invites to both candidate and recruiter.
    Returns the Google Calendar event ID, or empty string on failure.
    """
    service = _get_calendar_service()
    if not service:
        return ''

    application = interview.application
    candidate_email = application.candidate.user.email
    recruiter_email = application.job.recruiter.user.email
    job_title = application.job.title
    candidate_name = application.candidate.user.full_name or application.candidate.user.username

    start = interview.scheduled_at
    end = start + timedelta(minutes=interview.duration_minutes)

    description_parts = [
        f'SmartHire Interview — {job_title}',
        f'Candidate: {candidate_name}',
    ]
    if zoom_join_url:
        description_parts.append(f'Join Zoom: {zoom_join_url}')
    if interview.zoom_password:
        description_parts.append(f'Meeting Password: {interview.zoom_password}')

    event_body = {
        'summary': f'SmartHire Interview — {job_title}',
        'description': '\n'.join(description_parts),
        'start': {'dateTime': start.isoformat(), 'timeZone': 'Asia/Karachi'},
        'end': {'dateTime': end.isoformat(), 'timeZone': 'Asia/Karachi'},
        'attendees': [
            {'email': candidate_email, 'displayName': candidate_name},
            {'email': recruiter_email},
        ],
        'reminders': {
            'useDefault': False,
            'overrides': [
                {'method': 'email', 'minutes': 60},
                {'method': 'popup', 'minutes': 10},
            ],
        },
    }

    # Only add conferenceData request if there is no Zoom link (avoid duplicate meeting links)
    if not zoom_join_url:
        event_body['conferenceData'] = {
            'createRequest': {'requestId': f'smarthire-{interview.id}'}
        }

    try:
        event = service.events().insert(
            calendarId='primary',
            body=event_body,
            sendUpdates='all',
            # conferenceDataVersion required when conferenceData is set
            conferenceDataVersion=1 if not zoom_join_url else 0,
        ).execute()
        event_id = event.get('id', '')
        logger.info(f'Calendar event created: {event_id}')
        return event_id
    except Exception as e:
        logger.error(f'Google Calendar event creation failed: {e}')
        return ''


def delete_calendar_event(event_id: str) -> bool:
    """Delete a calendar event when an interview is cancelled."""
    service = _get_calendar_service()
    if not service or not event_id:
        return False
    try:
        service.events().delete(
            calendarId='primary',
            eventId=event_id,
            sendUpdates='all',
        ).execute()
        return True
    except Exception as e:
        logger.error(f'Calendar event deletion failed: {e}')
        return False
