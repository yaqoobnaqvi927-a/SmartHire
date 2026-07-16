from django.db import models
from users.models import RecruiterProfile, CandidateProfile

class JobPosting(models.Model):
    STATUS_CHOICES = (
        ('active', 'Active'),
        ('paused', 'Paused'),
        ('closed', 'Closed'),
    )
    TYPE_CHOICES = (
        ('onsite', 'On-site'),
        ('remote', 'Remote'),
        ('hybrid', 'Hybrid'),
    )
    
    recruiter = models.ForeignKey(RecruiterProfile, on_delete=models.CASCADE, related_name='jobs')
    title = models.CharField(max_length=255)
    company = models.CharField(max_length=255, default='Internal')
    description = models.TextField(blank=True, default='')
    required_skills_json = models.JSONField(default=list, blank=True)
    min_experience = models.IntegerField(default=0)
    degree_requirement = models.CharField(max_length=100, blank=True, default='')
    job_type = models.CharField(max_length=50, choices=TYPE_CHOICES, default='onsite')
    location = models.CharField(max_length=255, blank=True, default='')
    salary_range = models.CharField(max_length=100, blank=True, default='')
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='active')
    
    # Search & AI Matching
    vector_profile = models.JSONField(default=list, blank=True)
    search_keywords_index = models.TextField(blank=True, db_index=True)
    
    # Counters (denormalized for fast reads)
    applicant_count = models.IntegerField(default=0)
    ai_screened_count = models.IntegerField(default=0)
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def save(self, *args, **kwargs):
        skills_str = " ".join(self.required_skills_json) if isinstance(self.required_skills_json, list) else str(self.required_skills_json)
        self.search_keywords_index = f"{self.title} {self.company} {skills_str} {self.job_type} {self.location} {self.degree_requirement} {self.description}".lower()
        super().save(*args, **kwargs)

    def update_counts(self):
        """Recalculate applicant counts from actual application data."""
        self.applicant_count = self.applications.count()
        self.ai_screened_count = self.applications.filter(ai_match_score__gte=70).count()
        self.save(update_fields=['applicant_count', 'ai_screened_count'])

    def __str__(self):
        return f"{self.title} @ {self.company}"

class Application(models.Model):
    ATS_STAGES = (
        ('new', 'New'),
        ('screened', 'AI Screened'),
        ('interview', 'Interview'),
        ('offer', 'Offer'),
        ('hired', 'Hired'),
        ('rejected', 'Rejected'),
    )
    
    job = models.ForeignKey(JobPosting, on_delete=models.CASCADE, related_name='applications')
    candidate = models.ForeignKey(CandidateProfile, on_delete=models.CASCADE, related_name='applications')
    ai_match_score = models.FloatField(default=0.0)
    ats_status = models.CharField(max_length=50, choices=ATS_STAGES, default='new')
    skill_gap_analysis = models.JSONField(default=list, blank=True)
    cover_letter = models.TextField(blank=True, default='')
    applied_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('job', 'candidate')
        ordering = ['-applied_at']

    def save(self, *args, **kwargs):
        super().save(*args, **kwargs)
        # Update job counters when application changes
        self.job.update_counts()

    def __str__(self):
        return f"{self.candidate.user.username} -> {self.job.title} ({self.ats_status})"
