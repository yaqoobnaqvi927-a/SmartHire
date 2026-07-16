from django.db import models
from jobs.models import Application


class AIMatchReport(models.Model):
    """
    Persists the AI-generated skill gap analysis for a specific application.
    Generated on-demand and cached here for fast re-reads.
    """
    application = models.OneToOneField(
        Application,
        on_delete=models.CASCADE,
        related_name='ai_report',
    )
    match_score = models.FloatField(default=0.0, help_text="Overall AI match score (0-100)")
    skill_match_pct = models.FloatField(default=0.0, help_text="Percentage of required skills matched")
    experience_match = models.BooleanField(default=False, help_text="Whether candidate meets min experience")
    matched_skills = models.JSONField(default=list, help_text="List of skills candidate has that job requires")
    missing_skills = models.JSONField(default=list, help_text="List of required skills the candidate lacks")
    recommendation = models.TextField(blank=True, help_text="Gemini-generated career recommendation")
    generated_at = models.DateTimeField(auto_now=True, help_text="When this report was last generated/updated")

    class Meta:
        verbose_name = 'AI Match Report'
        verbose_name_plural = 'AI Match Reports'

    def __str__(self):
        return f'AI Report for Application #{self.application.id} — Score: {self.match_score}'
