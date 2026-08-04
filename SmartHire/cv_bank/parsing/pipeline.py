"""
Unified CV parsing pipeline combining fast PDF extraction, section segmentation,
taxonomy skill extraction, and lightweight spaCy NLP.
"""
from __future__ import annotations

import re
import logging
from typing import Dict, Any, List

from .pdf_extractor import extract_text_from_pdf
from .text_utils import clean_resume_text
from .sections import segment_resume_sections
from .skills import extract_skills_from_text
from .nlp_engine import analyze_text

logger = logging.getLogger(__name__)


def _extract_email(text: str) -> str:
    match = re.search(r'[\w\.-]+@[\w\.-]+\.\w+', text)
    return match.group(0) if match else ''


def _extract_phone(text: str) -> str:
    patterns = [
        r'\+?\d{1,3}[-.\s]?\(?\d{2,4}\)?[-.\s]?\d{3,4}[-.\s]?\d{3,4}',
        r'\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}',
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            phone = match.group(0).strip()
            if len(re.sub(r'\D', '', phone)) >= 7:
                return phone
    return ''


def _extract_url_pattern(text: str, pattern: str) -> str:
    match = re.search(pattern, text, re.IGNORECASE)
    if match:
        url = match.group(0).strip()
        if not url.startswith('http'):
            url = 'https://' + url
        return url
    return ''


def _extract_portfolio_url(text: str, exclude_urls: List[str]) -> str:
    pattern = r'(?:https?://)?(?:www\.)?[\w\-]+(?:\.[\w\-]+)+[/\w\-\.\?%&=]*'
    matches = re.findall(pattern, text, re.IGNORECASE)
    for match in matches:
        url = match.strip()
        if not url.startswith('http'):
            url = 'https://' + url
        url_lower = url.lower()
        if any(bad in url_lower for bad in ['linkedin.com', 'github.com', 'gmail.com', 'yahoo.com', 'outlook.com', 'hotmai.com', 'schema.org']):
            continue
        if url in exclude_urls or url + '/' in exclude_urls:
            continue
        if len(url) > 10:
            return url
    return ''


def _extract_name(text: str, doc: Any) -> str:
    if not text:
        return ''
    lines = text.split('\n')
    candidate_lines = lines[:4]

    if doc:
        person_ents = [ent.text.split('\n')[0].strip() for ent in doc.ents if ent.label_ == 'PERSON' and ent.start_char < 250]
        for name in person_ents:
            cleaned_name = re.sub(r'(?i)\b(email|phone|tel|address|location|curriculum|vitae|resume)\b.*', '', name)
            cleaned_name = re.sub(r'[^a-zA-Z\s]', '', cleaned_name).strip()
            words = cleaned_name.split()
            if 2 <= len(words) <= 3 and all(w[0].isupper() for w in words if w):
                return cleaned_name.title()

    for line in candidate_lines:
        line_clean = re.sub(r'(?i)\b(email|phone|tel|address|location|curriculum|vitae|resume)\b.*', '', line)
        line_clean = re.sub(r'[^a-zA-Z\s]', '', line_clean).strip()
        words = line_clean.split()
        if 2 <= len(words) <= 3 and all(w[0].isupper() for w in words if w):
            line_lower = line_clean.lower()
            if not any(kw in line_lower for kw in ['resume', 'cv', 'curriculum', 'page', 'profile', 'developer', 'engineer', 'manager']):
                return line_clean.title()

    return 'Candidate'


def _extract_location(text: str, doc: Any) -> str:
    if doc:
        gpe_ents = [ent.text.strip() for ent in doc.ents if ent.label_ in ('GPE', 'LOC') and ent.start_char < 500]
        if gpe_ents:
            return gpe_ents[0].title()

    location_patterns = [
        r'\b([A-Z][a-zA-Z\s]+,\s*[A-Z]{2})\b',
        r'\b([A-Z][a-zA-Z\s]+,\s*[A-Z][a-zA-Z\s]+)\b',
    ]
    for pattern in location_patterns:
        match = re.search(pattern, text[:500])
        if match:
            return match.group(1).strip()
    return ''


def extract_degree(text: str, edu_text: str = '') -> str:
    search_text = edu_text if edu_text else text
    search_text_lower = search_text.lower()

    if re.search(r'\bph\.?d\b|\bdoctorate\b|\bdoctor\s+of\s+philosophy\b', search_text_lower):
        return 'PhD'

    masters_pattern = r'\bm\.?s\.?\b|\bm\.?sc\b|\bmba\b|\bmaster\s*(?:of|s)?\b'
    if edu_text:
        if re.search(masters_pattern, search_text_lower):
            return 'Masters'
    else:
        if re.search(r'\bm\.?s\.?\b|\bm\.?sc\b|\bmba\b|\bmasters?\s+(?:in|of)\b|\bmaster\s+degree\b', search_text_lower):
            return 'Masters'

    bachelors_pattern = r'\bb\.?s\.?\b|\bbachelor\s*(?:s|of)?\b|\bb\.?tech\b|\bb\.?s\.?c\.?s\b|\bb\.?s\.?e\b|\bb\.?e\b|\bbba\b'
    if re.search(bachelors_pattern, search_text_lower):
        return 'Bachelors'

    if re.search(r'\bdiploma\b|\bassociate\b|\bdae\b', search_text_lower):
        return 'Diploma'

    return 'Not Specified'


def _parse_education_section(edu_text: str, doc: Any) -> List[Dict[str, str]]:
    if not edu_text:
        return []
    entries = []
    lines = edu_text.split('\n')
    current_entry: Dict[str, str] = {}
    
    for line in lines:
        line_clean = line.strip()
        if not line_clean:
            continue
        line_lower = line_clean.lower()
        if any(keyword in line_lower for keyword in ['university', 'college', 'institute', 'school', 'academy']):
            if current_entry and 'institution' in current_entry:
                entries.append(current_entry)
                current_entry = {}
            current_entry['institution'] = line_clean
        elif any(keyword in line_lower for keyword in ['bachelor', 'master', 'bs', 'ms', 'phd', 'degree', 'diploma']):
            current_entry['degree'] = line_clean
        year_match = re.search(r'\b(19|20)\d{2}\b', line_clean)
        if year_match and 'year' not in current_entry:
            current_entry['year'] = year_match.group(0)

    if current_entry and ('institution' in current_entry or 'degree' in current_entry):
        entries.append(current_entry)
        
    return entries


def _parse_experience_section(exp_text: str, doc: Any) -> List[Dict[str, str]]:
    if not exp_text:
        return []
    entries = []
    lines = exp_text.split('\n')
    current_entry: Dict[str, str] = {}

    for line in lines:
        line_clean = line.strip()
        if not line_clean:
            continue
        year_range = re.search(r'\b((?:19|20)\d{2})\s*[-–to\s]+\s*((?:19|20)\d{2}|present|current)\b', line_clean, re.IGNORECASE)
        if year_range:
            if current_entry and 'role' in current_entry:
                entries.append(current_entry)
                current_entry = {}
            current_entry['duration'] = year_range.group(0)
            parts = line_clean.split(year_range.group(0))
            if parts[0].strip():
                current_entry['role'] = parts[0].strip(' |-–,')
        elif not current_entry.get('role'):
            current_entry['role'] = line_clean
        elif not current_entry.get('company'):
            current_entry['company'] = line_clean

    if current_entry and 'role' in current_entry:
        entries.append(current_entry)

    return entries


def _estimate_experience_years(exp_list: List[Dict[str, str]], full_text: str) -> int:
    patterns = [
        r'(\d+)\+?\s*years?\s*(?:of\s*)?experience',
        r'(\d+)\+?\s*yrs?\s*exp',
        r'experience\s*[:\-]?\s*(\d+)\s*years?',
    ]
    for pattern in patterns:
        matches = re.findall(pattern, full_text.lower())
        for match in matches:
            try:
                years = int(match)
                if 0 < years < 40:
                    return years
            except (ValueError, TypeError):
                pass

    total_years = 0
    for exp in exp_list:
        duration = exp.get('duration', '')
        years_found = re.findall(r'\b(19|20)\d{2}\b', duration)
        if len(years_found) == 2:
            try:
                total_years += abs(int(years_found[1]) - int(years_found[0]))
            except ValueError:
                pass
    return min(total_years, 30) if total_years > 0 else 0


def _parse_certifications(cert_text: str, full_text: str) -> List[str]:
    source = cert_text if cert_text else full_text
    certs = []
    known_certs = ['AWS', 'Azure', 'PMP', 'Scrum Master', 'CISSP', 'CCNA', 'Google Cloud', 'CompTIA', 'Docker', 'Kubernetes']
    for cert in known_certs:
        if re.search(r'\b' + re.escape(cert) + r'\b', source, re.IGNORECASE):
            certs.append(cert)
    return certs


def _parse_languages(lang_text: str, full_text: str) -> List[str]:
    source = lang_text if lang_text else full_text
    langs = []
    known_langs = ['English', 'Spanish', 'French', 'German', 'Chinese', 'Urdu', 'Arabic', 'Hindi', 'Japanese']
    for lang in known_langs:
        if re.search(r'\b' + re.escape(lang) + r'\b', source, re.IGNORECASE):
            langs.append(lang)
    return langs


def _generate_local_bio(name: str, degree: str, skills: List[str], experience: int) -> str:
    skills_part = f" skilled in {', '.join(skills[:4])}" if skills else ""
    exp_part = f" with {experience} years of professional experience" if experience > 0 else " as a fresh graduate"
    deg_part = f" Holds a {degree} degree." if degree != "Not Specified" else ""
    return f"Professional candidate{skills_part}{exp_part}.{deg_part} Focused on delivering high-quality solutions and continuous career growth."


def parse_cv_text(text: str) -> Dict[str, Any]:
    """
    Main extraction pipeline entry point.
    """
    cleaned_text = clean_resume_text(text)

    email = _extract_email(cleaned_text)
    phone = _extract_phone(cleaned_text)
    linkedin = _extract_url_pattern(cleaned_text, r'(?:https?://)?(?:www\.)?linkedin\.com/in/[\w\-]+')
    github = _extract_url_pattern(cleaned_text, r'(?:https?://)?(?:www\.)?github\.com/[\w\-]+')
    portfolio = _extract_portfolio_url(cleaned_text, [linkedin, github])

    doc = analyze_text(cleaned_text)

    full_name = _extract_name(cleaned_text, doc)
    location = _extract_location(cleaned_text, doc)
    sections = segment_resume_sections(cleaned_text)

    skills = extract_skills_from_text(cleaned_text, skills_section=sections.get('skills', ''))
    education_list = _parse_education_section(sections.get('education', ''), doc)
    experience_list = _parse_experience_section(sections.get('experience', ''), doc)

    degree = extract_degree(cleaned_text, sections.get('education', ''))
    certifications = _parse_certifications(sections.get('certifications', ''), cleaned_text)
    languages = _parse_languages(sections.get('languages', ''), cleaned_text)

    total_experience = _estimate_experience_years(experience_list, cleaned_text)
    bio = _generate_local_bio(full_name, degree, skills, total_experience)

    return {
        'full_name': full_name,
        'email': email,
        'phone': phone,
        'location': location,
        'bio': bio,
        'skills': skills,
        'total_experience_years': total_experience,
        'degree': degree,
        'education': education_list,
        'work_experience': experience_list,
        'certifications': certifications,
        'languages': languages,
        'linkedin_url': linkedin,
        'github_url': github,
        'portfolio_url': portfolio,
    }
