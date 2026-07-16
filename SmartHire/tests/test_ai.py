"""
AI Engine Unit Tests
Covers skill gap analysis (normalize_skills, analyze_skill_gap) and
recommender functions (cosine_similarity_skills, compute_experience_score).
These tests run without a database — no @pytest.mark.django_db required.
"""
import pytest
from ai_engine.skill_gap import analyze_skill_gap, normalize_skills
from ai_engine.recommender import cosine_similarity_skills, compute_experience_score


class TestSkillGapAnalysis:
    """Unit tests for analyze_skill_gap and normalize_skills."""

    def test_perfect_match(self):
        """When candidate and job skills are identical, match should be 100%."""
        result = analyze_skill_gap(['Python', 'Django'], ['Python', 'Django'])
        assert result['skill_match_pct'] == 100.0
        assert result['missing_skills'] == []

    def test_no_match(self):
        """Completely disjoint skill sets should yield 0% match with all job skills missing."""
        result = analyze_skill_gap(['Java', 'Spring'], ['Python', 'Django'])
        assert result['skill_match_pct'] == 0.0
        assert len(result['missing_skills']) == 2

    def test_partial_match(self):
        """Partial skill overlap should produce a score between 0 and 100."""
        result = analyze_skill_gap(['Python', 'Java'], ['Python', 'Django', 'PostgreSQL'])
        assert 0 < result['skill_match_pct'] < 100
        # 'django' and 'postgresql' are missing; exact set depends on normalization
        assert 'django' in result['missing_skills']

    def test_empty_job_skills_returns_100(self):
        """If the job has no required skills, any candidate is a perfect match."""
        result = analyze_skill_gap(['Python', 'Django'], [])
        assert result['skill_match_pct'] == 100.0

    def test_normalize_skills_handles_string(self):
        """Comma-separated string input should be split and lowercased."""
        result = normalize_skills('Python, Django, REST API')
        assert 'python' in result
        assert 'django' in result

    def test_normalize_skills_handles_list(self):
        """List input with mixed case should all be lowercased."""
        result = normalize_skills(['Python', 'DJANGO', 'rest api'])
        assert 'python' in result
        assert 'django' in result

    def test_normalize_skills_deduplicates(self):
        """Duplicate skills (different case) should appear only once."""
        result = normalize_skills(['Python', 'python', 'PYTHON'])
        assert result.count('python') == 1

    def test_missing_skills_are_lowercase(self):
        """Missing skill entries returned by analyze_skill_gap must be lowercase."""
        result = analyze_skill_gap([], ['Python', 'Django'])
        for skill in result['missing_skills']:
            assert skill == skill.lower()


class TestRecommender:
    """Unit tests for cosine_similarity_skills and compute_experience_score."""

    def test_cosine_similarity_identical(self):
        """Identical skill lists should produce cosine similarity of 1.0."""
        score = cosine_similarity_skills(['python', 'django'], ['python', 'django'])
        assert score == pytest.approx(1.0, abs=0.01)

    def test_cosine_similarity_no_overlap(self):
        """Disjoint skill sets should produce cosine similarity of 0.0."""
        score = cosine_similarity_skills(['java', 'spring'], ['python', 'django'])
        assert score == 0.0

    def test_cosine_similarity_partial_overlap(self):
        """Partial overlap should produce a score strictly between 0 and 1."""
        score = cosine_similarity_skills(['python', 'java'], ['python', 'django'])
        assert 0.0 < score < 1.0

    def test_cosine_similarity_empty_both(self):
        """Both empty skill sets → vacuous similarity of 1.0."""
        score = cosine_similarity_skills([], [])
        assert score == 1.0

    def test_experience_score_exceeds_requirement(self):
        """Candidate with more experience than required should score 1.0."""
        score = compute_experience_score(candidate_exp=5, job_min_exp=2)
        assert score == 1.0

    def test_experience_score_below_requirement(self):
        """Candidate below minimum experience should score less than 0.5 when halfway there."""
        score = compute_experience_score(candidate_exp=1, job_min_exp=5)
        assert score < 0.5

    def test_experience_score_no_requirement(self):
        """Job with 0 minimum experience should always score 1.0 regardless of candidate exp."""
        score = compute_experience_score(candidate_exp=0, job_min_exp=0)
        assert score == 1.0

    def test_experience_score_exact_match(self):
        """Candidate with exactly the required experience should score 1.0."""
        score = compute_experience_score(candidate_exp=3, job_min_exp=3)
        assert score == 1.0

    def test_experience_score_zero_candidate(self):
        """Candidate with 0 experience vs. a requirement should score 0.0."""
        score = compute_experience_score(candidate_exp=0, job_min_exp=5)
        assert score == 0.0
