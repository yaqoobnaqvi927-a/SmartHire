"""
Optimized skill extraction using pre-compiled patterns and token lookup.
"""
from __future__ import annotations

import re
from typing import Dict, Iterable, List, Set, Tuple

from .skills_taxonomy import SKILL_TAXONOMY

# ── Build lookup structures once at import time ───────────────────────────────

def _display_name(skill: str) -> str:
    if len(skill) <= 3 and skill.replace('.', '').isalpha():
        return skill.upper()
    if skill in {'.net', 'c#', 'c++'}:
        return skill.upper() if skill != 'c++' else 'C++'
    return ' '.join(part.capitalize() if part not in {'js', 'ai', 'ml', 'ci', 'cd', 'api'}
                    else part.upper() for part in skill.split())


ALL_SKILLS_RAW: List[str] = []
for _skills in SKILL_TAXONOMY.values():
    ALL_SKILLS_RAW.extend(_skills)

# Single-token skills → display form
SINGLE_TOKEN_SKILLS: Dict[str, str] = {}
# Multi-word skills → compiled regex
MULTI_WORD_PATTERNS: List[Tuple[re.Pattern, str]] = []

for skill in ALL_SKILLS_RAW:
    display = _display_name(skill)
    if ' ' in skill or '.' in skill or '+' in skill:
        pattern = re.compile(r'(?<![a-z0-9])' + re.escape(skill) + r'(?![a-z0-9])', re.IGNORECASE)
        MULTI_WORD_PATTERNS.append((pattern, display))
    else:
        SINGLE_TOKEN_SKILLS[skill.lower()] = display

# Aliases commonly found on CVs
ALIASES = {
    'js': 'JavaScript',
    'ts': 'TypeScript',
    'py': 'Python',
    'postgres': 'PostgreSQL',
    'postgresql': 'PostgreSQL',
    'mongo': 'MongoDB',
    'k8s': 'Kubernetes',
    'ml': 'Machine Learning',
    'dl': 'Deep Learning',
    'nlp': 'NLP',
    'cv': 'Computer Vision',
    'ai': 'Generative AI',
    'reactjs': 'React',
    'vuejs': 'Vue.js',
    'nodejs': 'Node.js',
    'expressjs': 'Express',
    'nextjs': 'Next.js',
}
SINGLE_TOKEN_SKILLS.update({k: v for k, v in ALIASES.items()})

_LIST_SPLIT = re.compile(r'[,;|/•·\n]+')
_TOKEN_RE = re.compile(r'\b[a-z0-9+#.]+\b', re.IGNORECASE)


def _extract_listed_skills(section_text: str) -> Set[str]:
    """Parse comma/bullet-separated skill lists from a dedicated skills section."""
    found: Set[str] = set()
    if not section_text:
        return found

    for chunk in _LIST_SPLIT.split(section_text):
        token = chunk.strip().lower()
        if not token or len(token) < 2:
            continue
        if token in SINGLE_TOKEN_SKILLS:
            found.add(SINGLE_TOKEN_SKILLS[token])
            continue
        for pattern, display in MULTI_WORD_PATTERNS:
            if pattern.search(token):
                found.add(display)
                break
        else:
            # Title-case unknown short tokens that look like skills (2–30 chars)
            if 2 <= len(token) <= 30 and not token.isdigit():
                found.add(token.title())
    return found


def extract_skills_from_text(text: str, skills_section: str = '') -> List[str]:
    """
    Extract skills using taxonomy matching + explicit list parsing.

    Scans the dedicated skills section first (higher confidence), then full text.
    """
    found: Set[str] = set()

    if skills_section:
        found.update(_extract_listed_skills(skills_section))

    text_lower = text.lower()
    tokens = set(_TOKEN_RE.findall(text_lower))
    for token in tokens:
        if token in SINGLE_TOKEN_SKILLS:
            found.add(SINGLE_TOKEN_SKILLS[token])

    for pattern, display in MULTI_WORD_PATTERNS:
        if pattern.search(text_lower):
            found.add(display)

    # Preserve stable ordering: taxonomy hits first, then alphabetical
    return sorted(found, key=lambda s: (s.lower() not in {v.lower() for v in SINGLE_TOKEN_SKILLS.values()}, s.lower()))
