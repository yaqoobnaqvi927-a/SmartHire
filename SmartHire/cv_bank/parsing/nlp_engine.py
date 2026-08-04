"""
Lazy-loaded spaCy NLP engine with optimized pipe configuration.
"""
from __future__ import annotations

import logging
import threading
from typing import Any, Optional

logger = logging.getLogger(__name__)

_NLP = None
_NLP_LOCK = threading.Lock()
_SPACY_MAX_CHARS = 100_000
_NER_SAMPLE_CHARS = 8_000


def get_nlp():
    """Return a shared spaCy model instance, loading lazily on first use."""
    global _NLP
    if _NLP is not None:
        return _NLP

    with _NLP_LOCK:
        if _NLP is not None:
            return _NLP
        try:
            import spacy
            try:
                nlp = spacy.load('en_core_web_sm')
                # Disable heavy pipes not needed for entity extraction
                if hasattr(nlp, 'select_pipes'):
                    nlp.select_pipes(enable=['tok2vec', 'tagger', 'ner'])
                _NLP = nlp
                logger.info('spaCy en_core_web_sm loaded for CV parsing')
            except OSError:
                logger.warning('spaCy model en_core_web_sm not found — NLP features degraded')
                _NLP = False
        except ImportError:
            logger.warning('spaCy not installed — NLP features disabled')
            _NLP = False

    return _NLP if _NLP is not False else None


def analyze_text(text: str, max_chars: int = _SPACY_MAX_CHARS) -> Optional[Any]:
    """Run spaCy on truncated text for entity recognition."""
    nlp = get_nlp()
    if not nlp or not text:
        return None

    sample = text[:max_chars]
    try:
        return nlp(sample)
    except Exception as exc:
        logger.warning('spaCy processing failed: %s', exc)
        return None


def analyze_for_entities(text: str) -> Optional[Any]:
    """Fast NER pass on the header region of a CV (name, location)."""
    if not text:
        return None
    header = text[:_NER_SAMPLE_CHARS]
    return analyze_text(header, max_chars=_NER_SAMPLE_CHARS)
