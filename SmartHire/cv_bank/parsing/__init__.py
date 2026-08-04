"""
SmartHire CV parsing package — fast PDF extraction + NLP field extraction.
"""

from .pipeline import parse_cv_text
from .pdf_extractor import extract_text_from_pdf

__all__ = ['parse_cv_text', 'extract_text_from_pdf']
