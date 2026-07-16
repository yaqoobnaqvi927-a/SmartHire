from django.db import models
from django.contrib.auth import get_user_model

User = get_user_model()

class CV(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='cvs')
    file = models.FileField(upload_to='cvs/')
    uploaded_at = models.DateTimeField(auto_now_add=True)
    extracted_text = models.TextField(blank=True, help_text="Text extracted by parser")
    skills_extracted = models.TextField(blank=True, help_text="JSON or comma separated skills")
    parsed_experience = models.IntegerField(default=0)
    parsed_education = models.JSONField(default=list, blank=True)
    is_primary = models.BooleanField(default=False)

    def __str__(self):
        return f"{self.user.username} - CV {self.id}"
