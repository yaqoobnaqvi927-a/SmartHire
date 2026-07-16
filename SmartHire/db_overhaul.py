import os

files_to_update = {
    r"e:\FYP\SmartHire\users\models.py": """from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    ROLE_CHOICES = (
        ('student', 'Student'),
        ('recruiter', 'Recruiter'),
    )
    role_type = models.CharField(max_length=20, choices=ROLE_CHOICES, default='student')
    full_name = models.CharField(max_length=255, blank=True)

    def __str__(self):
        return f"{self.username} ({self.get_role_type_display()})"

class CandidateProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='candidate_profile')
    total_experience = models.IntegerField(default=0)
    extracted_skills_json = models.JSONField(default=list, blank=True)
    degree_extracted = models.CharField(max_length=255, blank=True)
    
    # NLP & AI Matching Fields (Preserved for functionality)
    cv_file_path = models.FileField(upload_to='candidate_cvs/', null=True, blank=True)
    vector_profile = models.JSONField(default=list, blank=True)
    search_keywords_index = models.TextField(blank=True, db_index=True)

    def save(self, *args, **kwargs):
        skills_str = " ".join(self.extracted_skills_json) if isinstance(self.extracted_skills_json, list) else str(self.extracted_skills_json)
        self.search_keywords_index = f"{self.user.full_name} {self.degree_extracted} {skills_str}".lower()
        super().save(*args, **kwargs)

class RecruiterProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='recruiter_profile')
    company_name = models.CharField(max_length=255)
    company_size = models.CharField(max_length=50, blank=True)
    industry = models.CharField(max_length=255, blank=True)
""",

    r"e:\FYP\SmartHire\jobs\models.py": """from django.db import models
from users.models import RecruiterProfile, CandidateProfile

class JobPosting(models.Model):
    recruiter = models.ForeignKey(RecruiterProfile, on_delete=models.CASCADE, related_name='jobs')
    title = models.CharField(max_length=255)
    required_skills_json = models.JSONField(default=list, blank=True)
    min_experience = models.IntegerField(default=0)
    job_type = models.CharField(max_length=50, default='onsite')
    status = models.CharField(max_length=50, default='active')
    
    vector_profile = models.JSONField(default=list, blank=True)
    search_keywords_index = models.TextField(blank=True, db_index=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def save(self, *args, **kwargs):
        skills_str = " ".join(self.required_skills_json) if isinstance(self.required_skills_json, list) else str(self.required_skills_json)
        self.search_keywords_index = f"{self.title} {skills_str} {self.job_type}".lower()
        super().save(*args, **kwargs)

class Application(models.Model):
    job = models.ForeignKey(JobPosting, on_delete=models.CASCADE, related_name='applications')
    candidate = models.ForeignKey(CandidateProfile, on_delete=models.CASCADE, related_name='applications')
    ai_match_score = models.FloatField(default=0.0)
    ats_status = models.CharField(max_length=50, default='new')
    skill_gap_analysis = models.JSONField(default=list, blank=True)
    applied_at = models.DateTimeField(auto_now_add=True)
""",

    r"e:\FYP\SmartHire\communications\models.py": """from django.db import models
from django.contrib.auth import get_user_model
from jobs.models import JobPosting, Application
from users.models import CandidateProfile, RecruiterProfile

User = get_user_model()

class Notification(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')
    title = models.CharField(max_length=255)
    message = models.TextField()
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
""",

    r"e:\FYP\SmartHire\interviews\models.py": """from django.db import models
from jobs.models import Application

class ScheduledInterview(models.Model):
    application = models.ForeignKey(Application, on_delete=models.CASCADE, related_name='interviews')
    zoom_meeting_link = models.URLField(blank=True)
    scheduled_datetime = models.DateTimeField()
    status = models.CharField(max_length=50, default='scheduled')

    def __str__(self):
        return f"Interview {self.id} for Application {self.application.id}"
"""
}

for filepath, content in files_to_update.items():
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Updated {filepath}")
