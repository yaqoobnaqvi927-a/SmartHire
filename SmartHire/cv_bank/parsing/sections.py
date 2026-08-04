"""
Resume section segmentation with expanded header patterns.
"""
from __future__ import annotations

import re
from typing import Dict, List

from .text_utils import normalize_header

SECTION_KEYS = (
    'summary',
    'experience',
    'education',
    'skills',
    'projects',
    'certifications',
    'languages',
    'awards',
    'publications',
    'references',
    'volunteer',
)

# (section_key, patterns) — order matters: more specific headers first
SECTION_PATTERNS: List[tuple] = [
    ('summary', [
        r'^professional\s+summary$', r'^executive\s+summary$', r'^career\s+summary$',
        r'^profile\s+summary$', r'^about(\s+me)?$', r'^objective$', r'^profile$',
        r'^summary$', r'^personal\s+statement$',
    ]),
    ('experience', [
        r'^professional\s+experience$', r'^work\s+experience$', r'^employment(\s+history)?$',
        r'^career\s+history$', r'^work\s+history$', r'^relevant\s+experience$',
        r'^industry\s+experience$', r'^internship(\s+experience)?$', r'^experience$',
    ]),
    ('education', [
        r'^education(\s+&?\s*qualifications?)?$', r'^academic(\s+background|s)?$',
        r'^qualifications?$', r'^educational\s+background$', r'^degrees?$',
    ]),
    ('skills', [
        r'^technical\s+skills$', r'^core\s+competencies$', r'^key\s+skills$',
        r'^skills\s+&?\s*tools$', r'^tools\s+&?\s*technologies$',
        r'^technologies$', r'^tech\s+stack$', r'^expertise$', r'^skill\s+set$',
        r'^competencies$', r'^skills$',
    ]),
    ('projects', [
        r'^projects?$', r'^personal\s+projects$', r'^academic\s+projects$',
        r'^key\s+projects$', r'^portfolio\s+projects$',
    ]),
    ('certifications', [
        r'^certifications?$', r'^licenses?\s+(&|and)\s+certifications?$',
        r'^professional\s+certifications?$', r'^courses?$', r'^training$',
        r'^accomplishments$', r'^achievements$',
    ]),
    ('languages', [
        r'^languages?$', r'^language\s+proficiency$',
    ]),
    ('awards', [
        r'^awards?(\\s+(&|and)\\s+honors?)?$', r'^honors?(\\s+(&|and)\\s+awards?)?$',
        r'^distinctions?$',
    ]),
    ('publications', [
        r'^publications?$', r'^research(\s+papers?)?$',
    ]),
    ('references', [
        r'^references?$', r'^referees?$',
    ]),
    ('volunteer', [
        r'^volunteer(\s+experience)?$', r'^community(\s+service)?$',
        r'^extracurricular(\s+activities)?$',
    ]),
]

_COMPILED_SECTION_RULES = [
    (key, [re.compile(pattern, re.IGNORECASE) for pattern in patterns])
    for key, patterns in SECTION_PATTERNS
]


def _is_section_header(line: str) -> str | None:
    normalized = normalize_header(line)
    if not normalized or len(normalized.split()) > 6:
        return None

    for key, patterns in _COMPILED_SECTION_RULES:
        for pattern in patterns:
            if pattern.match(normalized):
                return key

    # ALL-CAPS short headers: "SKILLS", "EXPERIENCE"
    if line.isupper() and 1 <= len(line.split()) <= 3:
        upper_map = {
            'SKILLS': 'skills',
            'EXPERIENCE': 'experience',
            'EDUCATION': 'education',
            'PROJECTS': 'projects',
            'SUMMARY': 'summary',
            'CERTIFICATIONS': 'certifications',
            'LANGUAGES': 'languages',
            'REFERENCES': 'references',
        }
        return upper_map.get(line.strip())

    return None


def segment_resume_sections(text: str) -> Dict[str, str]:
    """Split resume text into named sections based on header detection."""
    sections: Dict[str, str] = {key: '' for key in SECTION_KEYS}
    if not text:
        return sections

    current_section = None
    buckets: Dict[str, List[str]] = {key: [] for key in SECTION_KEYS}

    for line in text.split('\n'):
        header_key = _is_section_header(line)
        if header_key:
            current_section = header_key
            continue
        if current_section:
            buckets[current_section].append(line)

    for key in SECTION_KEYS:
        sections[key] = '\n'.join(buckets[key]).strip()

    return sections
