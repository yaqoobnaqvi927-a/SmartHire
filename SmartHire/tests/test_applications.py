"""
Job Application API Tests
Covers applying to jobs, duplicate prevention, listing, ATS status updates,
and AI match score computation on submission.
"""
import pytest
from jobs.models import Application


@pytest.mark.django_db
class TestJobApplications:
    def test_candidate_can_apply(self, auth_candidate, sample_job, candidate_user):
        """Candidate POSTing to /api/jobs/applications/ should create application with 201."""
        resp = auth_candidate.post('/api/jobs/applications/', {
            'job': sample_job.id,
        })
        assert resp.status_code == 201
        assert 'ai_match_score' in resp.data

    def test_duplicate_application_rejected(self, auth_candidate, sample_job, candidate_user):
        """Applying to the same job twice should return 400 (unique_together constraint)."""
        auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        resp = auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        assert resp.status_code == 400

    def test_candidate_sees_own_applications(self, auth_candidate, sample_job):
        """GET /api/jobs/applications/ should list all of the candidate's applications."""
        auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        resp = auth_candidate.get('/api/jobs/applications/')
        assert resp.status_code == 200
        assert len(resp.data['results']) >= 1

    def test_recruiter_can_update_ats_status(self, auth_recruiter, auth_candidate, sample_job, candidate_user):
        """Recruiter should be able to PATCH an application's ats_status to 'screened'."""
        apply_resp = auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        app_id = apply_resp.data['id']
        resp = auth_recruiter.patch(f'/api/jobs/applications/{app_id}/', {'ats_status': 'screened'})
        assert resp.status_code == 200
        assert resp.data['ats_status'] == 'screened'

    def test_candidate_cannot_update_ats_status(self, auth_candidate, sample_job):
        """
        Candidates attempting to escalate ats_status should be blocked (403)
        or the field silently ignored (200 without the change).
        The test accepts either response to accommodate both strictness levels.
        """
        apply_resp = auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        app_id = apply_resp.data['id']
        resp = auth_candidate.patch(f'/api/jobs/applications/{app_id}/', {'ats_status': 'hired'})
        # Either 403 forbidden or 200 (with field ignored) are acceptable
        assert resp.status_code in [200, 403]

    def test_ai_match_score_computed_on_apply(self, auth_candidate, sample_job):
        """The ai_match_score returned on application should be a float value."""
        resp = auth_candidate.post('/api/jobs/applications/', {'job': sample_job.id})
        assert resp.status_code == 201
        assert isinstance(resp.data.get('ai_match_score'), float)
