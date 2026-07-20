"""
SmartHire Test Suite — Shared Fixtures
Provides reusable pytest fixtures for users, profiles, and sample data.
All fixtures use the `db` marker (via django_db) and are compatible with
pytest-django's transactional test isolation.
"""
import pytest
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient

User = get_user_model()


@pytest.fixture
def api_client():
    """Return a DRF APIClient instance (unauthenticated)."""
    return APIClient()


@pytest.fixture
def candidate_user(db):
    """
    Create and return a student/candidate User with a linked CandidateProfile.
    The profile is pre-populated with enough data for AI matching tests.
    """
    user = User.objects.create_user(
        username='test_candidate',
        email='candidate@test.com',
        password='TestPass123!',
        role_type='student',
        full_name='Test Candidate'
    )
    from users.models import CandidateProfile
    CandidateProfile.objects.get_or_create(
        user=user,
        defaults={
            'extracted_skills_json': ['Python', 'Django', 'PostgreSQL'],
            'total_experience': 2,
            'degree_extracted': 'Bachelors'
        }
    )
    return user


@pytest.fixture
def recruiter_user(db):
    """
    Create and return a recruiter User with a linked RecruiterProfile.
    The profile has company_name set so setup_complete resolves True.
    """
    user = User.objects.create_user(
        username='test_recruiter',
        email='recruiter@test.com',
        password='TestPass123!',
        role_type='recruiter',
        full_name='Test Recruiter'
    )
    from users.models import RecruiterProfile
    RecruiterProfile.objects.get_or_create(
        user=user,
        defaults={'company_name': 'Test Corp', 'industry': 'Technology'}
    )
    return user


@pytest.fixture
def auth_candidate(candidate_user):
    """Return an APIClient force-authenticated as the candidate user."""
    from rest_framework.test import APIClient
    client = APIClient()
    client.force_authenticate(user=candidate_user)
    return client


@pytest.fixture
def auth_recruiter(recruiter_user):
    """Return an APIClient force-authenticated as the recruiter user."""
    from rest_framework.test import APIClient
    client = APIClient()
    client.force_authenticate(user=recruiter_user)
    return client


@pytest.fixture
def sample_job(db, recruiter_user):
    """
    Create and return an active JobPosting owned by the recruiter fixture.
    Skills chosen to overlap with the candidate_user fixture for score testing.
    """
    from jobs.models import JobPosting
    return JobPosting.objects.create(
        recruiter=recruiter_user.recruiter_profile,
        title='Django Backend Developer',
        company='Test Corp',
        description='Build REST APIs with Django.',
        required_skills_json=['Python', 'Django', 'PostgreSQL', 'Redis'],
        min_experience=1,
        job_type='remote',
        status='active',
    )
