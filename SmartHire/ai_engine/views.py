"""
SmartHire AI Engine — API Views
Exposes skill gap analysis, job recommendations, and per-job match scoring.
"""
import logging

from django.shortcuts import get_object_or_404
from rest_framework import permissions, status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response

from jobs.models import Application, JobPosting
from .models import AIMatchReport
from .skill_gap import analyze_skill_gap
from .recommender import cosine_similarity_skills, compute_experience_score, rank_jobs_for_candidate

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------

def _parse_skills(raw) -> list:
    """Coerce a skills value (list or comma-string) to a clean list."""
    if isinstance(raw, str):
        return [s.strip() for s in raw.split(',') if s.strip()]
    if isinstance(raw, list):
        return list(raw)
    return []


# ---------------------------------------------------------------------------
# Views
# ---------------------------------------------------------------------------

@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def skill_gap_view(request, application_id):
    """
    GET /api/ai/skill-gap/<application_id>/

    Returns a full skill-gap analysis for a specific application.
    Results are cached in AIMatchReport for subsequent fast reads.

    Access:
      - The applying candidate can view their own report.
      - The recruiter who posted the job can view any applicant's report.
    """
    application = get_object_or_404(
        Application.objects.select_related('candidate__user', 'job__recruiter__user'),
        id=application_id,
    )

    user = request.user
    is_candidate = (
        hasattr(user, 'candidate_profile')
        and application.candidate == user.candidate_profile
    )
    is_recruiter = (
        hasattr(user, 'recruiter_profile')
        and application.job.recruiter == user.recruiter_profile
    )

    if not (is_candidate or is_recruiter):
        return Response(
            {'error': 'You do not have permission to view this report.'},
            status=status.HTTP_403_FORBIDDEN,
        )

    candidate_skills = _parse_skills(application.candidate.extracted_skills_json)
    job_skills = _parse_skills(application.job.required_skills_json)

    report = analyze_skill_gap(
        candidate_skills=candidate_skills,
        job_skills=job_skills,
        candidate_experience=application.candidate.total_experience or 0,
        job_min_experience=application.job.min_experience or 0,
    )

    # Persist / update the cached report in the database
    AIMatchReport.objects.update_or_create(
        application=application,
        defaults={
            'match_score': application.ai_match_score,
            'skill_match_pct': report['skill_match_pct'],
            'experience_match': report['experience_match'],
            'matched_skills': report['matched_skills'],
            'missing_skills': report['missing_skills'],
            'recommendation': report['recommendation'],
        },
    )

    candidate_name = (
        application.candidate.user.full_name
        or application.candidate.user.username
    )

    return Response({
        'application_id': application_id,
        'job_title': application.job.title,
        'candidate_name': candidate_name,
        'overall_match_score': application.ai_match_score,
        'skill_match_pct': report['skill_match_pct'],
        'experience_match': report['experience_match'],
        'matched_skills': report['matched_skills'],
        'missing_skills': report['missing_skills'],
        'recommendation': report['recommendation'],
    })


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def recommended_jobs_view(request):
    """
    GET /api/ai/recommended-jobs/

    Returns the top-20 AI-ranked active job postings for the authenticated candidate,
    each annotated with a match_percentage field.
    Only accessible to users with a candidate_profile.
    """
    if not hasattr(request.user, 'candidate_profile'):
        return Response(
            {'error': 'Only candidates can access job recommendations.'},
            status=status.HTTP_403_FORBIDDEN,
        )

    profile = request.user.candidate_profile
    active_jobs = JobPosting.objects.filter(status='active').select_related('recruiter').order_by('-created_at')

    ranked = rank_jobs_for_candidate(profile, active_jobs)

    # Import here to avoid circular imports at module load time
    from jobs.serializers import JobPostingSerializer

    data = []
    for item in ranked[:20]:  # Return top 20 matches
        serialized = JobPostingSerializer(item['job']).data
        serialized['match_percentage'] = item['match_percentage']
        data.append(serialized)

    return Response(data)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def match_score_for_job_view(request, job_id):
    """
    GET /api/ai/match-score/<job_id>/

    Computes and returns the AI match score between the authenticated candidate
    and a specific job posting, without requiring the candidate to apply first.
    Only accessible to users with a candidate_profile.
    """
    if not hasattr(request.user, 'candidate_profile'):
        return Response(
            {'error': 'Only candidates can check match scores.'},
            status=status.HTTP_403_FORBIDDEN,
        )

    job = get_object_or_404(JobPosting, id=job_id)
    profile = request.user.candidate_profile

    candidate_skills = _parse_skills(profile.extracted_skills_json)
    job_skills = _parse_skills(job.required_skills_json)

    # Full skill-gap report
    report = analyze_skill_gap(
        candidate_skills=candidate_skills,
        job_skills=job_skills,
        candidate_experience=profile.total_experience or 0,
        job_min_experience=job.min_experience or 0,
    )

    # Compute the composite match percentage (same weights as the recommender)
    skill_s = cosine_similarity_skills(candidate_skills, job_skills)
    exp_s = compute_experience_score(profile.total_experience or 0, job.min_experience or 0)
    match_pct = round((skill_s * 0.7 + exp_s * 0.3) * 100, 1)

    return Response({
        'job_id': job_id,
        'job_title': job.title,
        'company': job.company,
        'match_percentage': match_pct,
        'skill_match_pct': report['skill_match_pct'],
        'experience_match': report['experience_match'],
        'matched_skills': report['matched_skills'],
        'missing_skills': report['missing_skills'],
        'recommendation': report['recommendation'],
    })
