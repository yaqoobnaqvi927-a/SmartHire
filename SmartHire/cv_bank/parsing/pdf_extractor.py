"""
Fast PDF text extraction with PyMuPDF (primary) and PyPDF2 (fallback).
Supports parallel page extraction for multi-page documents.
"""
from __future__ import annotations

import logging
from concurrent.futures import ThreadPoolExecutor
from io import BytesIO
from typing import BinaryIO, Union

logger = logging.getLogger(__name__)

FileInput = Union[str, bytes, BinaryIO]

try:
    import fitz  # PyMuPDF

    PYMUPDF_AVAILABLE = True
except ImportError:
    fitz = None
    PYMUPDF_AVAILABLE = False

try:
    import PyPDF2

    PYPDF2_AVAILABLE = True
except ImportError:
    PyPDF2 = None
    PYPDF2_AVAILABLE = False


def _read_bytes(pdf_file: FileInput) -> bytes:
    if isinstance(pdf_file, bytes):
        return pdf_file
    if isinstance(pdf_file, str):
        with open(pdf_file, 'rb') as handle:
            return handle.read()
    data = pdf_file.read()
    if hasattr(pdf_file, 'seek'):
        pdf_file.seek(0)
    return data


def _extract_page_text_pymupdf(doc: 'fitz.Document', page_index: int) -> str:
    page = doc.load_page(page_index)
    # "text" preserves reading order; faster than "blocks" for typical CVs
    return page.get_text('text') or ''


def _extract_with_pymupdf(data: bytes) -> str:
    doc = fitz.open(stream=data, filetype='pdf')
    try:
        page_count = doc.page_count
        if page_count == 0:
            return ''
        if page_count == 1:
            return _extract_page_text_pymupdf(doc, 0).strip()

        max_workers = min(4, page_count)
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            pages = list(
                executor.map(
                    lambda idx: _extract_page_text_pymupdf(doc, idx),
                    range(page_count),
                )
            )
        return '\n'.join(part.strip() for part in pages if part.strip())
    finally:
        doc.close()


def _extract_with_pypdf2(data: bytes) -> str:
    reader = PyPDF2.PdfReader(BytesIO(data))
    chunks = []
    for page in reader.pages:
        page_text = page.extract_text()
        if page_text:
            chunks.append(page_text.strip())
    return '\n'.join(chunks)


def extract_text_from_pdf(pdf_file: FileInput) -> str:
    """
    Extract plain text from a PDF using the fastest available backend.

    Order: PyMuPDF (fitz) → PyPDF2 → empty string.
    Resets seekable file objects after reading.
    """
    try:
        data = _read_bytes(pdf_file)
    except Exception as exc:
        logger.error('Failed to read PDF input: %s', exc)
        return ''

    if not data:
        return ''

    if PYMUPDF_AVAILABLE:
        try:
            text = _extract_with_pymupdf(data)
            if text.strip():
                return text
        except Exception as exc:
            logger.warning('PyMuPDF extraction failed, falling back: %s', exc)

    if PYPDF2_AVAILABLE:
        try:
            return _extract_with_pypdf2(data)
        except Exception as exc:
            logger.error('PyPDF2 extraction failed: %s', exc)

    logger.error('No PDF backend available (install pymupdf or PyPDF2)')
    return ''
