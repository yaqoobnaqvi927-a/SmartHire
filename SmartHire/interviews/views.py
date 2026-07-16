from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.utils import timezone
from .models import Interview
from .serializers import InterviewSerializer
from .zoom_service import create_zoom_meeting, delete_zoom_meeting
from .calendar_service import create_calendar_event, delete_calendar_event
from jobs.models import Application


class InterviewViewSet(viewsets.ModelViewSet):
    serializer_class = InterviewSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'candidate_profile'):
            return Interview.objects.filter(
                application__candidate=user.candidate_profile
            ).select_related(
                'application__job',
                'application__candidate__user',
                'application__job__recruiter__user',
            )
        if hasattr(user, 'recruiter_profile'):
            return Interview.objects.filter(
                application__job__recruiter=user.recruiter_profile
            ).select_related(
                'application__job',
                'application__candidate__user',
            )
        return Interview.objects.none()

    def perform_create(self, serializer):
        """Schedule an interview with automatic Zoom meeting creation and calendar invite."""
        if not hasattr(self.request.user, 'recruiter_profile'):
            raise permissions.exceptions.PermissionDenied('Only recruiters can schedule interviews.')

        interview = serializer.save()
        self._setup_meeting_and_notify(interview)

    def _setup_meeting_and_notify(self, interview):
        """
        Orchestrates post-save side-effects:
        1. Creates a Zoom meeting and persists meeting details.
        2. Creates a Google Calendar event with attendee invites.
        3. Updates the application ATS status to 'interview'.
        4. Fires a real-time notification to the candidate.
        """
        application = interview.application
        job_title = application.job.title

        # --- Zoom Meeting ---
        zoom_data = create_zoom_meeting(
            topic=f'SmartHire Interview — {job_title}',
            start_time=interview.scheduled_at,
            duration_minutes=interview.duration_minutes,
            agenda=f'Interview for {job_title} position via SmartHire platform.',
        )

        if zoom_data:
            interview.zoom_meeting_id = zoom_data['meeting_id']
            interview.meeting_link = zoom_data['join_url']
            interview.zoom_host_url = zoom_data['host_url']
            interview.zoom_password = zoom_data['password']
            interview.save(update_fields=['zoom_meeting_id', 'meeting_link', 'zoom_host_url', 'zoom_password'])

        # --- Google Calendar Event ---
        event_id = create_calendar_event(interview, zoom_join_url=interview.meeting_link)
        if event_id:
            interview.google_calendar_event_id = event_id
            interview.save(update_fields=['google_calendar_event_id'])

        # --- Update Application ATS Status ---
        application.ats_status = 'interview'
        application.save(update_fields=['ats_status'])

        # --- Candidate Notification ---
        try:
            from core.utils import notify_user
            scheduled_str = interview.scheduled_at.strftime('%b %d, %Y at %I:%M %p')
            notify_user(
                user=application.candidate.user,
                title='Interview Scheduled 🎯',
                message=(
                    f'Your interview for {job_title} is scheduled for {scheduled_str}. '
                    f'Join: {interview.meeting_link}'
                ),
                notif_type='interview',
                ref_id=interview.id,
            )
        except Exception as e:
            # Notifications are non-critical — log and continue
            print(f'Notification error (non-critical): {e}')

    @action(detail=False, methods=['get'])
    def upcoming(self, request):
        """Return the next 10 upcoming scheduled interviews for the requesting user."""
        queryset = self.get_queryset().filter(
            scheduled_at__gte=timezone.now(),
            status='scheduled',
        ).order_by('scheduled_at')[:10]
        serializer = self.get_serializer(queryset, many=True)
        return Response(serializer.data)

    @action(detail=True, methods=['patch'])
    def cancel(self, request, pk=None):
        """
        Cancel an interview.
        Deletes the associated Zoom meeting and Google Calendar event,
        then notifies the candidate.
        """
        interview = self.get_object()

        if interview.status == 'completed':
            return Response(
                {'error': 'Cannot cancel a completed interview.'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Cancel Zoom meeting if one was created
        if interview.zoom_meeting_id:
            delete_zoom_meeting(interview.zoom_meeting_id)

        # Remove Google Calendar event if one was created
        if interview.google_calendar_event_id:
            delete_calendar_event(interview.google_calendar_event_id)

        interview.status = 'cancelled'
        interview.save(update_fields=['status'])

        # Notify candidate
        try:
            from core.utils import notify_user
            notify_user(
                user=interview.application.candidate.user,
                title='Interview Cancelled',
                message=(
                    f'Your interview for {interview.application.job.title} has been cancelled. '
                    f'Please check your messages for further details.'
                ),
                notif_type='interview',
                ref_id=interview.id,
            )
        except Exception as e:
            print(f'Notification error (non-critical): {e}')

        return Response(self.get_serializer(interview).data)

    @action(detail=True, methods=['patch'])
    def complete(self, request, pk=None):
        """
        Mark an interview as completed (recruiter only).
        Optionally accepts updated notes via request body.
        """
        if not hasattr(request.user, 'recruiter_profile'):
            return Response(
                {'error': 'Only recruiters can mark interviews as complete.'},
                status=status.HTTP_403_FORBIDDEN,
            )

        interview = self.get_object()
        interview.status = 'completed'
        interview.notes = request.data.get('notes', interview.notes)
        interview.save(update_fields=['status', 'notes'])

        return Response(self.get_serializer(interview).data)
