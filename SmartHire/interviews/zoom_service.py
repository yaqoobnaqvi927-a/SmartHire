"""
SmartHire Zoom Integration — Server-to-Server OAuth
Creates and manages Zoom meetings for scheduled interviews.
"""
import os
import base64
import logging
from datetime import datetime

import requests

logger = logging.getLogger(__name__)

ZOOM_TOKEN_URL = 'https://zoom.us/oauth/token'
ZOOM_API_BASE = 'https://api.zoom.us/v2'


def _get_zoom_access_token() -> str | None:
    """
    Obtain a short-lived Zoom access token using Server-to-Server OAuth.
    Returns the access token string, or None if credentials are missing.
    """
    account_id = os.environ.get('ZOOM_ACCOUNT_ID', '')
    client_id = os.environ.get('ZOOM_CLIENT_ID', '')
    client_secret = os.environ.get('ZOOM_CLIENT_SECRET', '')

    if not all([account_id, client_id, client_secret]):
        logger.warning('Zoom credentials not configured. Set ZOOM_ACCOUNT_ID, ZOOM_CLIENT_ID, ZOOM_CLIENT_SECRET.')
        return None

    creds = f'{client_id}:{client_secret}'
    encoded = base64.b64encode(creds.encode()).decode()

    try:
        resp = requests.post(
            ZOOM_TOKEN_URL,
            headers={
                'Authorization': f'Basic {encoded}',
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            data={
                'grant_type': 'account_credentials',
                'account_id': account_id,
            },
            timeout=10,
        )
        resp.raise_for_status()
        return resp.json().get('access_token')
    except Exception as e:
        logger.error(f'Zoom token fetch failed: {e}')
        return None


def create_zoom_meeting(topic: str, start_time: datetime, duration_minutes: int = 60, agenda: str = '') -> dict | None:
    """
    Create a scheduled Zoom meeting.
    Returns a dict with meeting_id, join_url, host_url, password — or None on failure.
    """
    token = _get_zoom_access_token()
    if not token:
        return None

    payload = {
        'topic': topic,
        'type': 2,  # Scheduled meeting
        'start_time': start_time.strftime('%Y-%m-%dT%H:%M:%SZ'),
        'duration': duration_minutes,
        'agenda': agenda,
        'timezone': 'Asia/Karachi',
        'settings': {
            'host_video': True,
            'participant_video': True,
            'join_before_host': False,
            'waiting_room': True,
            'mute_upon_entry': False,
            'auto_recording': 'none',
        },
    }

    try:
        resp = requests.post(
            f'{ZOOM_API_BASE}/users/me/meetings',
            headers={
                'Authorization': f'Bearer {token}',
                'Content-Type': 'application/json',
            },
            json=payload,
            timeout=15,
        )
        resp.raise_for_status()
        data = resp.json()
        return {
            'meeting_id': str(data['id']),
            'join_url': data['join_url'],
            'host_url': data.get('start_url', ''),
            'password': data.get('password', ''),
        }
    except Exception as e:
        logger.error(f'Zoom meeting creation failed: {e}')
        return None


def delete_zoom_meeting(meeting_id: str) -> bool:
    """Cancel a Zoom meeting."""
    token = _get_zoom_access_token()
    if not token or not meeting_id:
        return False
    try:
        resp = requests.delete(
            f'{ZOOM_API_BASE}/meetings/{meeting_id}',
            headers={'Authorization': f'Bearer {token}'},
            timeout=10,
        )
        return resp.status_code in (204, 200)
    except Exception as e:
        logger.error(f'Zoom meeting deletion failed: {e}')
        return False
