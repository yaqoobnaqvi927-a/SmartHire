"""
Text normalization helpers for CV parsing.
"""
from __future__ import annotations

import re
import unicodedata

# Headers often appear as === SKILLS === or **Skills**
_HEADER_DECORATION = re.compile(r'^[\s*=_\-#•·]+|[\s*=_\-#•·]+$')
_BULLET_PREFIX = re.compile(r'^[\u2022\u2023\u25E6\u2043\u2219\-\*\+>\u2013\u2014]\s*')
_MULTI_SPACE = re.compile(r'[ \t]+')
_MULTI_NEWLINE = re.compile(r'\n{3,}')


def clean_resume_text(text: str) -> str:
    """Normalize unicode, whitespace, and strip decorative header markers."""
    if not text:
        return ''

    normalized = unicodedata.normalize('NFKC', text)
    normalized = normalized.replace('\r\n', '\n').replace('\r', '\n')

    lines = []
    for raw_line in normalized.split('\n'):
        line = _BULLET_PREFIX.sub('', raw_line.strip())
        line = _HEADER_DECORATION.sub('', line).strip()
        line = _MULTI_SPACE.sub(' ', line)
        if line:
            lines.append(line)

    return _MULTI_NEWLINE.sub('\n\n', '\n'.join(lines))


def normalize_header(line: str) -> str:
    """Lowercase header line for section matching."""
    line = _HEADER_DECORATION.sub('', line.strip().lower())
    return _MULTI_SPACE.sub(' ', line)
