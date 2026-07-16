from django.db import models
from jobs.models import Application


class Interview(models.Model):
    TYPE_CHOICES = (
        ('video', 'Video Call'),
        ('phone', 'Phone'),
        ('onsite', 'On-site'),
        ('technical', 'Technical Assessment'),
    )
    STATUS_CHOICES = (
        ('scheduled', 'Scheduled'),
        ('completed', 'Completed'),
        ('cancelled', 'Cancelled'),
        ('rescheduled', 'Rescheduled'),
        ('no_show', 'No Show'),
    )

    application = models.ForeignKey(Application, on_delete=models.CASCADE, related_name='interviews')
    scheduled_at = models.DateTimeField()
    interview_type = models.CharField(max_length=50, choices=TYPE_CHOICES, default='video')

    # Meeting fields (Zoom or manual link)
    meeting_link = models.URLField(blank=True)  # Join URL for candidate
    zoom_meeting_id = models.CharField(max_length=100, blank=True)
    zoom_host_url = models.URLField(blank=True)  # Start URL for recruiter
    zoom_password = models.CharField(max_length=100, blank=True)

    # Google Calendar
    google_calendar_event_id = models.CharField(max_length=255, blank=True)

    notes = models.TextField(blank=True)
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='scheduled')
    duration_minutes = models.IntegerField(default=60)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['scheduled_at']

    def __str__(self):
        return f"Interview: {self.application.candidate.user.username} for {self.application.job.title} on {self.scheduled_at}"
