from rest_framework import serializers
from .models import Interview
from jobs.serializers import ApplicationSerializer


class InterviewSerializer(serializers.ModelSerializer):
    # Nested read-only detail of the full application
    application_detail = ApplicationSerializer(source='application', read_only=True)
    # Convenience flat fields for quick UI rendering
    candidate_name = serializers.SerializerMethodField()
    job_title = serializers.SerializerMethodField()

    class Meta:
        model = Interview
        fields = [
            'id',
            'application',
            'application_detail',
            'scheduled_at',
            'interview_type',
            'duration_minutes',
            'meeting_link',
            'zoom_meeting_id',
            'zoom_host_url',
            'zoom_password',
            'google_calendar_event_id',
            'notes',
            'status',
            'candidate_name',
            'job_title',
            'created_at',
            'updated_at',
        ]
        read_only_fields = [
            'zoom_meeting_id',
            'zoom_host_url',
            'zoom_password',
            'google_calendar_event_id',
            'created_at',
            'updated_at',
        ]

    def get_candidate_name(self, obj) -> str:
        user = obj.application.candidate.user
        return user.full_name or user.username

    def get_job_title(self, obj) -> str:
        return obj.application.job.title
