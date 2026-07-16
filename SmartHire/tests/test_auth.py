"""
Authentication API Tests
Covers registration, JWT login, role data in token, and profile endpoint security.
"""
import pytest


@pytest.mark.django_db
class TestAuthentication:
    def test_candidate_registration(self, api_client):
        """POST /api/users/register/ with student role should return 201."""
        resp = api_client.post('/api/users/register/', {
            'username': 'new_candidate',
            'email': 'new@test.com',
            'password': 'TestPass123!',
            'role_type': 'student',
            'full_name': 'New Candidate',
        })
        assert resp.status_code == 201

    def test_recruiter_registration(self, api_client):
        """POST /api/users/register/ with recruiter role should return 201."""
        resp = api_client.post('/api/users/register/', {
            'username': 'new_recruiter',
            'email': 'recruiter_new@test.com',
            'password': 'TestPass123!',
            'role_type': 'recruiter',
            'full_name': 'New Recruiter',
        })
        assert resp.status_code == 201

    def test_login_returns_jwt_tokens(self, api_client, candidate_user):
        """Successful login should return access + refresh tokens and role."""
        resp = api_client.post('/api/users/login/', {
            'username': 'test_candidate',
            'password': 'TestPass123!',
        })
        assert resp.status_code == 200
        assert 'access' in resp.data
        assert 'refresh' in resp.data
        assert resp.data['role'] == 'student'

    def test_login_includes_role(self, api_client, recruiter_user):
        """Login response must include the user's role_type field."""
        resp = api_client.post('/api/users/login/', {
            'username': 'test_recruiter',
            'password': 'TestPass123!',
        })
        assert resp.status_code == 200
        assert resp.data['role'] == 'recruiter'

    def test_profile_endpoint_requires_auth(self, api_client):
        """GET /api/users/profile/ without token must return 401."""
        resp = api_client.get('/api/users/profile/')
        assert resp.status_code == 401

    def test_profile_returns_role_data(self, auth_candidate, candidate_user):
        """Authenticated GET /api/users/profile/ should include role_type."""
        resp = auth_candidate.get('/api/users/profile/')
        assert resp.status_code == 200
        assert resp.data['role_type'] == 'student'

    def test_invalid_login_returns_401(self, api_client, candidate_user):
        """Wrong password must be rejected with 401."""
        resp = api_client.post('/api/users/login/', {
            'username': 'test_candidate',
            'password': 'WrongPassword',
        })
        assert resp.status_code == 401
