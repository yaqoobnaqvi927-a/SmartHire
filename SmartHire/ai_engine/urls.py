from django.urls import path
from . import views

urlpatterns = [
    # GET /api/ai/skill-gap/<application_id>/
    path('skill-gap/<int:application_id>/', views.skill_gap_view, name='skill_gap'),

    # GET /api/ai/recommended-jobs/
    path('recommended-jobs/', views.recommended_jobs_view, name='recommended_jobs'),

    # GET /api/ai/match-score/<job_id>/
    path('match-score/<int:job_id>/', views.match_score_for_job_view, name='match_score'),
]
