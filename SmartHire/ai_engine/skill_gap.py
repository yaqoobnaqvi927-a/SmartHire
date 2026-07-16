"""
SmartHire Skill Gap Analysis — Powered by Google Gemini API
Compares candidate skills against job requirements and generates AI recommendations.
Falls back to rule-based logic if Gemini API is unavailable or unconfigured.
"""
import logging
import os

logger = logging.getLogger(__name__)

# Guard the import so the app boots even without the package installed
try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except ImportError:
    GEMINI_AVAILABLE = False
    logger.warning(
        'google-generativeai not installed. Gemini recommendations disabled.'
    )

GEMINI_API_KEY = os.environ.get('GEMINI_API_KEY', '')
if GEMINI_AVAILABLE and GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def normalize_skills(skills) -> list:
    """
    Normalize a skills value to a lowercase, stripped list of strings.
    Accepts a comma-separated string, a Python list, or None.
    Deduplicates while preserving relative order.
    """
    if isinstance(skills, str):
        raw = [s.strip() for s in skills.split(',') if s.strip()]
    elif isinstance(skills, list):
        raw = [str(s).strip() for s in skills if str(s).strip()]
    else:
        return []

    seen: set = set()
    result: list = []
    for skill in raw:
        lower = skill.lower()
        if lower not in seen:
            seen.add(lower)
            result.append(lower)
    return result


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def analyze_skill_gap(
    candidate_skills,
    job_skills,
    candidate_experience: int = 0,
    job_min_experience: int = 0,
) -> dict:
    """
    Analyze the gap between a candidate's skills and a job's requirements.

    Args:
        candidate_skills:     list or comma-string of candidate skills.
        job_skills:           list or comma-string of required job skills.
        candidate_experience: total years of experience the candidate holds.
        job_min_experience:   minimum years required by the job posting.

    Returns:
        dict with keys:
            matched_skills    – list of skills the candidate has that match the job
            missing_skills    – list of required skills the candidate lacks
            skill_match_pct   – float percentage 0–100
            experience_match  – bool
            recommendation    – human-readable advice string (Gemini or rule-based)
    """
    c_skills = normalize_skills(candidate_skills)
    j_skills = normalize_skills(job_skills)

    # Edge case: job has no skill requirements
    if not j_skills:
        return {
            'matched_skills': c_skills,
            'missing_skills': [],
            'skill_match_pct': 100.0,
            'experience_match': candidate_experience >= job_min_experience,
            'recommendation': 'No specific technical skills are required for this position.',
        }

    c_set = set(c_skills)
    j_set = set(j_skills)

    matched = sorted(c_set & j_set)
    missing = sorted(j_set - c_set)
    skill_match_pct = round((len(matched) / len(j_set)) * 100, 1)
    experience_match = candidate_experience >= job_min_experience

    recommendation = _generate_recommendation_gemini(
        candidate_skills=c_skills,
        job_skills=j_skills,
        missing=missing,
        skill_match_pct=skill_match_pct,
        experience_match=experience_match,
        job_min_experience=job_min_experience,
        candidate_experience=candidate_experience,
    )

    return {
        'matched_skills': matched,
        'missing_skills': missing,
        'skill_match_pct': skill_match_pct,
        'experience_match': experience_match,
        'recommendation': recommendation,
    }


# ---------------------------------------------------------------------------
# Internal: Gemini recommendation generator
# ---------------------------------------------------------------------------

def _generate_recommendation_gemini(
    candidate_skills: list,
    job_skills: list,
    missing: list,
    skill_match_pct: float,
    experience_match: bool,
    job_min_experience: int,
    candidate_experience: int,
) -> str:
    """
    Use Gemini Flash to write a concise, human-readable career recommendation.
    Falls back to a rule-based string if Gemini is unavailable or the call fails.
    """
    if GEMINI_AVAILABLE and GEMINI_API_KEY:
        try:
            model = genai.GenerativeModel('gemini-2.0-flash')
            prompt = f"""You are a professional career advisor for SmartHire, a tech recruitment platform.

Candidate Profile:
- Skills: {', '.join(candidate_skills[:15])}
- Years of Experience: {candidate_experience}

Job Requirements:
- Required Skills: {', '.join(job_skills)}
- Minimum Experience Required: {job_min_experience} years

Analysis Results:
- Skill Match: {skill_match_pct}%
- Missing Skills: {', '.join(missing) if missing else 'None'}
- Experience Requirement Met: {'Yes' if experience_match else 'No'}

Write a concise, professional 2-3 sentence recommendation for the candidate. Be specific about:
1. Their key strengths for this role.
2. What to improve or learn (if any missing skills exist).
3. One actionable next step (e.g., a specific online course, certification, or side project).

Write in paragraph form only. Do NOT use bullet points or markdown."""
            response = model.generate_content(prompt)
            return response.text.strip()
        except Exception as exc:
            logger.warning(f'Gemini skill-gap recommendation failed: {exc}')

    # ---- Rule-based fallback ----
    return _rule_based_recommendation(skill_match_pct, missing, experience_match)


def _rule_based_recommendation(
    skill_match_pct: float,
    missing: list,
    experience_match: bool,
) -> str:
    """Deterministic fallback recommendation when Gemini is unavailable."""
    exp_note = (
        ' Your experience level meets the job requirements.'
        if experience_match
        else ' Consider highlighting transferable skills and project work to compensate for the experience gap.'
    )

    if skill_match_pct >= 80:
        return (
            f"You are a strong match for this role with {skill_match_pct}% skill alignment."
            f"{exp_note}"
        )

    if missing:
        top_missing = ', '.join(missing[:3])
        return (
            f"You match {skill_match_pct}% of the required skills."
            f" Focus on learning {top_missing} to strengthen your application —"
            " consider an online course or a hands-on side project using these technologies."
            f"{exp_note}"
        )

    return (
        f"Your profile matches {skill_match_pct}% of this role's requirements."
        " Review the job description carefully and tailor your CV and cover letter to highlight"
        f" your most relevant experience.{exp_note}"
    )
