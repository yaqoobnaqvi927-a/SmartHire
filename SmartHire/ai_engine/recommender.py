"""
AI Engine — Job Recommender Module
Provides cosine-similarity and experience scoring functions used for
AI-powered job-candidate matching. Operates purely on Python built-ins
with no external ML library dependency.
"""
from __future__ import annotations
import math
from typing import Union


def _build_skill_vector(skills: list[str], universe: set[str]) -> list[float]:
    """
    Build a binary presence vector for `skills` over the given `universe`.

    Args:
        skills:   List of normalized (lowercase) skill strings.
        universe: The full vocabulary set (union of both skill lists).

    Returns:
        list[float] — 1.0 if skill in `skills`, else 0.0, for each term in the
        sorted universe.
    """
    skills_set = set(skills)
    return [1.0 if term in skills_set else 0.0 for term in sorted(universe)]


def _dot_product(vec1: list[float], vec2: list[float]) -> float:
    """Compute the dot product of two equal-length vectors."""
    return sum(a * b for a, b in zip(vec1, vec2))


def _magnitude(vec: list[float]) -> float:
    """Compute the Euclidean magnitude (L2 norm) of a vector."""
    return math.sqrt(sum(v ** 2 for v in vec))


def cosine_similarity_skills(
    skills_a: Union[list[str], str],
    skills_b: Union[list[str], str],
) -> float:
    """
    Compute cosine similarity between two skill sets.

    Each skill set is converted to a binary presence vector over the union
    vocabulary, then cosine similarity is computed.

    Args:
        skills_a: First skill set (list of strings or comma-separated string).
        skills_b: Second skill set (list of strings or comma-separated string).

    Returns:
        float in [0.0, 1.0] — 1.0 means identical sets, 0.0 means no overlap.

    Notes:
        - Input strings are lowercased and stripped before comparison.
        - If both sets are empty, returns 1.0 (vacuous similarity).
        - If only one set is empty, returns 0.0.
    """
    # Normalize inputs to lowercase lists
    def _to_list(s: Union[list, str]) -> list[str]:
        if isinstance(s, str):
            return [x.strip().lower() for x in s.split(',') if x.strip()]
        return [str(x).strip().lower() for x in s if str(x).strip()]

    list_a = _to_list(skills_a)
    list_b = _to_list(skills_b)

    # Union vocabulary
    universe = set(list_a) | set(list_b)

    if not universe:
        # Both sets empty → vacuous perfect similarity
        return 1.0

    vec_a = _build_skill_vector(list_a, universe)
    vec_b = _build_skill_vector(list_b, universe)

    mag_a = _magnitude(vec_a)
    mag_b = _magnitude(vec_b)

    if mag_a == 0.0 or mag_b == 0.0:
        return 0.0

    similarity = _dot_product(vec_a, vec_b) / (mag_a * mag_b)
    # Clamp to [0, 1] to handle any floating-point edge cases
    return max(0.0, min(1.0, similarity))


def compute_experience_score(
    candidate_exp: Union[int, float],
    job_min_exp: Union[int, float],
) -> float:
    """
    Compute a normalized experience match score in [0.0, 1.0].

    Scoring logic:
        - If job requires 0 years → score is 1.0 (no barrier).
        - If candidate meets or exceeds requirement → score is 1.0.
        - If candidate is below requirement → linear penalty:
              score = candidate_exp / job_min_exp

    Args:
        candidate_exp: Years of experience the candidate has (>= 0).
        job_min_exp:   Minimum years of experience the job requires (>= 0).

    Returns:
        float in [0.0, 1.0].
    """
    # Clamp negatives to zero
    candidate_exp = max(0.0, float(candidate_exp))
    job_min_exp = max(0.0, float(job_min_exp))

    if job_min_exp == 0.0:
        # No experience requirement → always a full match
        return 1.0

    if candidate_exp >= job_min_exp:
        return 1.0

    # Linear interpolation: 0 exp → 0.0, job_min_exp → 1.0
    return round(candidate_exp / job_min_exp, 4)


def compute_composite_match_score(
    candidate_skills: Union[list[str], str],
    job_skills: Union[list[str], str],
    candidate_exp: Union[int, float] = 0,
    job_min_exp: Union[int, float] = 0,
    weights: dict | None = None,
) -> float:
    """
    Compute a weighted composite AI match score combining skill similarity
    and experience match.

    Default weights:
        - skill_similarity : 0.75
        - experience_score : 0.25

    Args:
        candidate_skills: Candidate's skills (list or comma-string).
        job_skills:       Job's required skills (list or comma-string).
        candidate_exp:    Candidate years of experience.
        job_min_exp:      Job minimum years of experience required.
        weights:          Optional dict overriding default weight allocation.
                          Keys: 'skill', 'experience'. Must sum to 1.0.

    Returns:
        float in [0.0, 100.0] — percentage match score.
    """
    if weights is None:
        weights = {'skill': 0.75, 'experience': 0.25}

    skill_score = cosine_similarity_skills(candidate_skills, job_skills)
    exp_score = compute_experience_score(candidate_exp, job_min_exp)

    composite = (weights['skill'] * skill_score) + (weights['experience'] * exp_score)
    # Convert to percentage and round to 2 decimal places
    return round(min(composite * 100, 100.0), 2)


def rank_jobs_for_candidate(candidate_profile, job_queryset) -> list:
    """
    Rank a queryset of JobPosting objects for a given candidate profile.

    Returns a list of dicts sorted by match_percentage descending:
        [{'job': <JobPosting>, 'match_percentage': float}, ...]
    """
    candidate_skills = candidate_profile.extracted_skills_json or []
    candidate_exp = candidate_profile.total_experience or 0

    if isinstance(candidate_skills, str):
        candidate_skills = [s.strip() for s in candidate_skills.split(',') if s.strip()]

    results = []
    for job in job_queryset:
        job_skills = job.required_skills_json or []
        if isinstance(job_skills, str):
            job_skills = [s.strip() for s in job_skills.split(',') if s.strip()]

        match_pct = compute_composite_match_score(
            candidate_skills=candidate_skills,
            job_skills=job_skills,
            candidate_exp=candidate_exp,
            job_min_exp=job.min_experience or 0,
        )
        results.append({'job': job, 'match_percentage': match_pct})

    return sorted(results, key=lambda x: x['match_percentage'], reverse=True)


def rank_candidates_for_job(job, candidate_queryset) -> list:
    """
    Rank a queryset of CandidateProfile objects for a given job.

    Returns a list of dicts sorted by match_percentage descending:
        [{'candidate': <CandidateProfile>, 'match_percentage': float}, ...]
    """
    job_skills = job.required_skills_json or []
    if isinstance(job_skills, str):
        job_skills = [s.strip() for s in job_skills.split(',') if s.strip()]

    results = []
    for candidate in candidate_queryset:
        c_skills = candidate.extracted_skills_json or []
        if isinstance(c_skills, str):
            c_skills = [s.strip() for s in c_skills.split(',') if s.strip()]

        match_pct = compute_composite_match_score(
            candidate_skills=c_skills,
            job_skills=job_skills,
            candidate_exp=candidate.total_experience or 0,
            job_min_exp=job.min_experience or 0,
        )
        results.append({'candidate': candidate, 'match_percentage': match_pct})

    return sorted(results, key=lambda x: x['match_percentage'], reverse=True)
