from django.contrib.auth.models import AbstractUser
from django.db import models
from django.utils import timezone

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
    
    # Core AI-Parsed Fields
    total_experience = models.IntegerField(default=0)
    extracted_skills_json = models.JSONField(default=list, blank=True)
    degree_extracted = models.CharField(max_length=255, blank=True)
    
    # Rich Profile Fields (populated by Gemini AI parser)
    bio = models.TextField(blank=True, help_text="AI-generated professional summary")
    location = models.CharField(max_length=255, blank=True)
    github_url = models.URLField(blank=True)
    linkedin_url = models.URLField(blank=True)
    portfolio_url = models.URLField(blank=True)
    education_json = models.JSONField(default=list, blank=True, help_text='[{"institution":"","degree":"","field":"","year":""}]')
    work_experience_json = models.JSONField(default=list, blank=True, help_text='[{"company":"","role":"","duration":"","description":""}]')
    certifications_json = models.JSONField(default=list, blank=True, help_text='["AWS Certified", ...]')
    profile_completeness = models.IntegerField(default=0, help_text="0-100 score based on filled sections")
    
    # Search & Matching Infrastructure
    cv_file_path = models.FileField(upload_to='candidate_cvs/', null=True, blank=True)
    vector_profile = models.JSONField(default=list, blank=True)
    search_keywords_index = models.TextField(blank=True, db_index=True)
    
    # Activity & Visibility
    is_searchable = models.BooleanField(default=True)
    last_active = models.DateTimeField(default=timezone.now)
    
    # Stats
    profile_views_count = models.IntegerField(default=0)
    
    def save(self, *args, **kwargs):
        # Build search index from all profile data
        skills_str = " ".join(self.extracted_skills_json) if isinstance(self.extracted_skills_json, list) else str(self.extracted_skills_json)
        certs_str = " ".join(self.certifications_json) if isinstance(self.certifications_json, list) else ""
        self.search_keywords_index = f"{self.user.full_name} {self.degree_extracted} {skills_str} {self.bio} {self.location} {certs_str}".lower()
        
        # Calculate profile completeness
        score = 0
        if self.extracted_skills_json: score += 25
        if self.degree_extracted: score += 15
        if self.total_experience > 0: score += 15
        if self.bio: score += 10
        if self.education_json: score += 10
        if self.work_experience_json: score += 10
        if self.location: score += 5
        if self.cv_file_path: score += 5
        if self.linkedin_url or self.github_url: score += 5
        self.profile_completeness = min(score, 100)
        
        super().save(*args, **kwargs)

    def __str__(self):
        return f"Profile: {self.user.username}"

class RecruiterProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='recruiter_profile')
    company_name = models.CharField(max_length=255)
    company_size = models.CharField(max_length=50, blank=True)
    industry = models.CharField(max_length=255, blank=True)
    company_logo_url = models.URLField(blank=True)
    company_website = models.URLField(blank=True)

    def __str__(self):
        return f"Recruiter: {self.user.username} @ {self.company_name}"
