from django.db import models
from django.contrib.auth import get_user_model
from jobs.models import JobPosting, Application
from users.models import CandidateProfile, RecruiterProfile

User = get_user_model()

class Notification(models.Model):
    NOTIF_TYPES = (
        ('general', 'General'),
        ('application', 'Application'),
        ('interview', 'Interview'),
        ('job_alert', 'Job Alert'),
        ('message', 'Message'),
    )
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')
    title = models.CharField(max_length=255)
    message = models.TextField()
    notif_type = models.CharField(max_length=50, choices=NOTIF_TYPES, default='general')
    ref_id = models.BigIntegerField(null=True, blank=True, help_text='ID of the related object (application, interview, etc.)')
    is_read = models.BooleanField(default=False)
    timestamp = models.DateTimeField(auto_now_add=True)


class ChatThread(models.Model):
    job = models.ForeignKey(JobPosting, on_delete=models.CASCADE, related_name='threads')
    candidate = models.ForeignKey(CandidateProfile, on_delete=models.CASCADE, related_name='threads')
    recruiter = models.ForeignKey(RecruiterProfile, on_delete=models.CASCADE, related_name='threads')
    created_at = models.DateTimeField(auto_now_add=True)

class ChatMessage(models.Model):
    thread = models.ForeignKey(ChatThread, on_delete=models.CASCADE, related_name='messages')
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sent_messages')
    content = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        ordering = ['timestamp']


class DeviceToken(models.Model):
    """
    Stores FCM push notification tokens for each user device.
    A single user may have multiple devices (phone + tablet, etc.).
    Invalid tokens are automatically purged by fcm_service.py after delivery failures.
    """
    DEVICE_TYPE_CHOICES = (
        ('android', 'Android'),
        ('ios', 'iOS'),
        ('web', 'Web'),
    )

    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='device_tokens',
    )
    fcm_token = models.TextField(
        help_text='Firebase Cloud Messaging registration token from the device.',
    )
    device_type = models.CharField(
        max_length=20,
        choices=DEVICE_TYPE_CHOICES,
        default='android',
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        # Prevent duplicate (user, token) pairs — each token is unique per user
        unique_together = ('user', 'fcm_token')
        verbose_name = 'Device Token'
        verbose_name_plural = 'Device Tokens'

    def __str__(self):
        return f'{self.user.username} — {self.device_type} token'
