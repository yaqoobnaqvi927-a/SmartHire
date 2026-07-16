"""
SmartHire Local NLP CV Parser — Powered by spaCy + PyPDF2
Extracts structured profile data from uploaded CV/resume PDFs entirely offline.
"""
import re
import json
import math
import os
from collections import Counter

import PyPDF2

# spaCy NLP model integration
try:
    import spacy
    try:
        nlp = spacy.load("en_core_web_sm")
    except Exception:
        # Fallback if download command failed or model not found
        nlp = None
except ImportError:
    nlp = None


def extract_text_from_pdf(pdf_file):
    """Extract raw text from a PDF file."""
    text = ""
    try:
        reader = PyPDF2.PdfReader(pdf_file)
        for page in reader.pages:
            page_text = page.extract_text()
            if page_text:
                text += page_text + "\n"
    except Exception as e:
        print(f"Error reading PDF: {e}")
    return text


def clean_resume_text(text):
    """Normalize whitespace and remove empty lines."""
    if not text:
        return ""
    lines = [line.strip() for line in text.split('\n')]
    lines = [line for line in lines if line]
    return '\n'.join(lines)


def _extract_name(text, doc):
    """Extract candidate name using spaCy PERSON entities and heuristics."""
    if not text:
        return ""
    lines = text.split('\n')
    candidate_lines = lines[:4]
    
    if doc:
        person_ents = [ent.text.strip() for ent in doc.ents if ent.label_ == "PERSON" and ent.start_char < 250]
        for name in person_ents:
            cleaned_name = re.sub(r'[^a-zA-Z\s]', '', name).strip()
            words = cleaned_name.split()
            if 2 <= len(words) <= 3 and all(w[0].isupper() for w in words if w):
                return cleaned_name
                
    for line in candidate_lines:
        line_clean = re.sub(r'[^a-zA-Z\s]', '', line).strip()
        words = line_clean.split()
        if 2 <= len(words) <= 3 and all(w[0].isupper() for w in words if w):
            lower_line = line_clean.lower()
            if not any(k in lower_line for k in ['resume', 'cv', 'curriculum', 'email', 'phone', 'portfolio', 'page']):
                return line_clean
                
    return "Candidate Name"


def _extract_phone(text):
    """Extract phone number using international/local patterns."""
    pattern = r'(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}|\+?92[-.\s]?\d{3}[-.\s]?\d{7}'
    matches = re.findall(pattern, text)
    return matches[0].strip() if matches else ""


def _extract_url_pattern(text, pattern):
    """Extract URL based on regex pattern."""
    match = re.search(pattern, text, re.IGNORECASE)
    return match.group(0) if match else ""


def _extract_portfolio_url(text, exclude_urls):
    """Extract portfolio URL while ignoring LinkedIn/GitHub links."""
    urls = re.findall(r'https?://[a-zA-Z0-9./\-]+', text)
    exclude_set = {url.lower() for url in exclude_urls if url}
    for url in urls:
        url_lower = url.lower()
        if not any(exc in url_lower for exc in exclude_set) and not any(k in url_lower for k in ['linkedin', 'github', 'google', 'drive.google', 'dropbox']):
            return url
    return ""


def _extract_location(text, doc):
    """Extract location using GPE entities or city patterns."""
    if doc:
        gpes = [ent.text.strip() for ent in doc.ents if ent.label_ == "GPE" and ent.start_char < 500]
        if gpes:
            unique_gpes = list(dict.fromkeys(gpes))
            return ", ".join(unique_gpes[:2])
            
    lines = text.split('\n')[:6]
    for line in lines:
        match = re.search(r'([A-Z][a-z]+(?:\s[A-Z][a-z]+)*),\s*([A-Z][a-z]+)', line)
        if match:
            return match.group(0)
    return ""


def segment_resume_sections(text):
    """Segment resume text into structured sections based on headers."""
    sections = {
        'education': '',
        'experience': '',
        'skills': '',
        'certifications': '',
        'languages': '',
        'projects': '',
        'summary': ''
    }
    
    section_patterns = {
        'education': [r'\beducation\b', r'\bacademics\b', r'\bacademics?\s+background\b', r'\bacademics?\s+history\b', r'\bacademics?\s+qualification\b'],
        'experience': [r'\bwork\s+experience\b', r'\bprofessional\s+experience\b', r'\bexperience\b', r'\bemployment\s+history\b', r'\bwork\s+history\b', r'\bcareer\s+history\b'],
        'skills': [r'\bskills\b', r'\btechnical\s+skills\b', r'\bcore\s+competencies\b', r'\bexpertise\b', r'\bskill\s+set\b'],
        'certifications': [r'\bcertifications\b', r'\bcertificates\b', r'\bcertification\b', r'\baccomplishments\b', r'\bprofessional\s+certifications\b'],
        'languages': [r'\blanguages\b', r'\blanguage\b'],
        'projects': [r'\bprojects\b', r'\bacademics?\s+projects\b', r'\bpersonal\s+projects\b'],
        'summary': [r'\bsummary\b', r'\bprofessional\s+summary\b', r'\babout\s+me\b', r'\bobjective\b', r'\bprofile\b']
    }
    
    lines = text.split('\n')
    current_section = None
    section_lines = {key: [] for key in sections.keys()}
    
    for line in lines:
        line_clean = line.strip().lower()
        found_header = False
        for key, patterns in section_patterns.items():
            for pattern in patterns:
                if re.match(pattern, line_clean) and len(line_clean.split()) <= 4:
                    current_section = key
                    found_header = True
                    break
            if found_header:
                break
        
        if found_header:
            continue
            
        if current_section:
            section_lines[current_section].append(line)
            
    for key in sections.keys():
        sections[key] = '\n'.join(section_lines[key])
        
    return sections


def parse_education_section(edu_text, doc):
    """Extract education entries like degree, field, year, institution."""
    if not edu_text:
        return []
    
    entries = []
    blocks = edu_text.split('\n\n')
    if len(blocks) <= 1:
        # Fallback if no double newlines
        blocks = []
        current_block = []
        for line in edu_text.split('\n'):
            line = line.strip()
            if not line:
                continue
            is_header = any(kw in line.lower() for kw in ['university', 'college', 'institute', 'school', 'academy', 'bachelor', 'master', 'phd', 'diploma']) and len(line.split()) < 10
            if is_header and current_block:
                blocks.append('\n'.join(current_block))
                current_block = [line]
            else:
                current_block.append(line)
        if current_block:
            blocks.append('\n'.join(current_block))
            
    for block in blocks:
        lines = [line.strip() for line in block.split('\n') if line.strip()]
        if not lines:
            continue
            
        institution = ""
        degree = ""
        field = ""
        year = ""
        
        degree_patterns = {
            'Bachelors': [r'\bb\.?s\.?\b', r'\bbachelor\b', r'\bb\.?tech\b', r'\bb\.?s\.?c\.?s\b', r'\bb\.?s\.?e\b', r'\bb\.?e\b', r'\bbba\b'],
            'Masters': [r'\bm\.?s\.?\b', r'\bmaster\b', r'\bm\.?tech\b', r'\bm\.?s\.?c\b', r'\bmba\b'],
            'PhD': [r'\bph\.?d\b', r'\bdoctorate\b', r'\bdoctor\s+of\s+philosophy\b'],
            'Diploma': [r'\bdiploma\b', r'\bassociate\b']
        }
        
        for line in lines:
            line_lower = line.lower()
            
            # Check for year
            year_match = re.search(r'\b(20\d{2})\b', line)
            if year_match and not year:
                year = year_match.group(1)
                
            # Check for degree & field
            found_degree = None
            for deg, patterns in degree_patterns.items():
                for pattern in patterns:
                    if re.search(pattern, line_lower):
                        found_degree = deg
                        field_match = re.search(r'(?:in|of)\s+([a-zA-Z\s]+)', line, re.IGNORECASE)
                        if field_match:
                            field = field_match.group(1).strip()
                        break
                if found_degree:
                    break
            
            if found_degree and not degree:
                degree = found_degree
                
            # Check for institution
            inst_keywords = ['university', 'college', 'institute', 'school', 'academy', 'quest']
            is_inst = any(kw in line_lower for kw in inst_keywords)
            if is_inst and not institution:
                institution = line
                
        if institution or degree:
            entries.append({
                'institution': institution or "University",
                'degree': degree or "Degree",
                'field': field or "Field of Study",
                'year': year or "N/A"
            })
            
    return entries


def parse_experience_section(exp_text, doc):
    """Extract experience entries like company, role, duration, description."""
    if not exp_text:
        return []
        
    entries = []
    blocks = exp_text.split('\n\n')
    if len(blocks) <= 1:
        # Fallback if there are no double newlines
        blocks = []
        current_block = []
        for line in exp_text.split('\n'):
            line = line.strip()
            if not line:
                continue
            is_header = any(kw in line.lower() for kw in ['developer', 'engineer', 'manager', 'analyst', 'intern', 'ltd', 'inc', 'corp', 'pvt']) and len(line.split()) < 7
            if is_header and current_block:
                blocks.append('\n'.join(current_block))
                current_block = [line]
            else:
                current_block.append(line)
        if current_block:
            blocks.append('\n'.join(current_block))
            
    for block in blocks:
        lines = [line.strip() for line in block.split('\n') if line.strip()]
        if not lines:
            continue
            
        company = ""
        role = ""
        duration = ""
        desc_lines = []
        
        for line in lines:
            line_lower = line.lower()
            duration_match = re.search(r'\b(20\d{2}|present)\b.*?-.*?\b(20\d{2}|present)\b', line_lower)
            if not duration_match:
                duration_match = re.search(r'\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+\d{4}\b.*?-.*?\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|present)[a-z]*\s*\d{0,4}\b', line_lower)
            
            if duration_match and not duration:
                duration = line
                continue
                
            is_company = any(kw in line_lower for kw in [' ltd', ' inc', ' corp', ' pvt', ' llc', ' limited', 'labs', 'devcorp', 'codelabs'])
            if is_company and not company:
                company = line
                if '|' in line:
                    parts = line.split('|')
                    role = parts[0].strip()
                    company = parts[1].strip()
                elif ',' in line and len(line.split(',')) == 2:
                    parts = line.split(',')
                    role = parts[0].strip()
                    company = parts[1].strip()
                continue
                
            role_keywords = ['developer', 'engineer', 'manager', 'analyst', 'designer', 'intern', 'lead', 'architect', 'programmer']
            is_role = any(kw in line_lower for kw in role_keywords) and len(line.split()) < 6
            if is_role and not role:
                role = line
                continue
                
            desc_lines.append(line)
            
        if company or role:
            if not company: company = "Company"
            if not role: role = "Job Title"
            if not duration: duration = "Duration"
            
            entries.append({
                'company': company,
                'role': role,
                'duration': duration,
                'description': '\n'.join(desc_lines)
            })
            
    return entries


def estimate_experience_years(experience_list, text):
    """Estimate total experience years from job list and fallback to text patterns."""
    total_months = 0
    year_pattern = r'\b(\d{4})\b'
    
    for exp in experience_list:
        duration = exp.get('duration', '').lower()
        if not duration:
            continue
            
        years_found = re.findall(year_pattern, duration)
        if len(years_found) == 2:
            try:
                start = int(years_found[0])
                end = int(years_found[1])
                total_months += (end - start) * 12
            except ValueError:
                pass
        elif len(years_found) == 1 and 'present' in duration:
            try:
                start = int(years_found[0])
                total_months += (2026 - start) * 12
            except ValueError:
                pass
                
    if total_months > 0:
        return max(1, round(total_months / 12))
        
    return extract_experience_years(text)


def parse_certifications(cert_text, full_text):
    """Extract list of certifications."""
    if cert_text:
        certs = [line.strip() for line in cert_text.split('\n') if len(line.strip()) > 3]
        return certs[:8]
    
    cert_keywords = ['aws', 'ccna', 'pmp', 'certified', 'scrum master', 'ceh', 'itil']
    found = []
    for line in full_text.split('\n'):
        line_lower = line.lower()
        if any(kw in line_lower for kw in cert_keywords) and len(line.split()) < 8:
            found.append(line.strip())
    return list(set(found))[:6]


def parse_languages(lang_text, full_text):
    """Extract list of languages."""
    if lang_text:
        langs = [line.replace(':', '').replace('-', '').strip() for line in lang_text.split('\n') if len(line.strip()) > 2]
        return langs[:5]
        
    common_langs = ['english', 'urdu', 'punjabi', 'sindhi', 'spanish', 'french', 'german', 'chinese', 'arabic']
    found = []
    text_lower = full_text.lower()
    for lang in common_langs:
        pattern = r'\b' + lang + r'\b'
        if re.search(pattern, text_lower):
            found.append(lang.title())
    return found


def generate_local_bio(name, degree, skills, experience):
    """Generate a clean, professional career bio candidate summary."""
    skills_part = f" skilled in {', '.join(skills[:4])}" if skills else ""
    exp_part = f" with {experience} years of professional experience" if experience > 0 else " as a fresh graduate"
    deg_part = f"Holds a {degree} degree." if degree != "Not Specified" else ""
    return f"Professional candidate{skills_part}{exp_part}. {deg_part} Focused on delivering high-quality solutions and continuous career growth."


def parse_cv_locally_with_nlp(text):
    """
    Perform local offline parsing using spaCy NLP + regular expressions + section segmentation.
    Highly optimized for fast, accurate local extraction.
    """
    cleaned_text = clean_resume_text(text)
    
    email = _extract_email(cleaned_text)
    phone = _extract_phone(cleaned_text)
    linkedin = _extract_url_pattern(cleaned_text, r'(?:https?://)?(?:www\.)?linkedin\.com/in/[\w\-]+')
    github = _extract_url_pattern(cleaned_text, r'(?:https?://)?(?:www\.)?github\.com/[\w\-]+')
    portfolio = _extract_portfolio_url(cleaned_text, [linkedin, github])
    
    doc = nlp(cleaned_text[:12000]) if nlp else None
    
    full_name = _extract_name(cleaned_text, doc)
    location = _extract_location(cleaned_text, doc)
    skills = extract_skills_from_text(cleaned_text)
    sections = segment_resume_sections(cleaned_text)
    
    education_list = parse_education_section(sections.get('education', ''), doc)
    experience_list = parse_experience_section(sections.get('experience', ''), doc)
    
    # Extract degree using education section specifically to avoid global false positives
    degree = extract_degree(cleaned_text, sections.get('education', ''))
    certifications = parse_certifications(sections.get('certifications', ''), cleaned_text)
    languages = parse_languages(sections.get('languages', ''), cleaned_text)
    
    total_experience = estimate_experience_years(experience_list, cleaned_text)
    bio = generate_local_bio(full_name, degree, skills, total_experience)
    
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


def parse_cv_with_gemini(text):
    """
    Intelligently parse CV text into structured data.
    Replaced Gemini entirely with a local spaCy NLP parser.
    """
    return parse_cv_locally_with_nlp(text)


def parse_cv_with_regex(text):
    """Fallback regex-based parser when Gemini is unavailable."""
    return parse_cv_locally_with_nlp(text)


def _extract_email(text):
    match = re.search(r'[\w\.-]+@[\w\.-]+\.\w+', text)
    return match.group(0) if match else ''



# ═══════════════════════════════════════════════════
# SKILL EXTRACTION (Expanded Taxonomy — 200+ skills)
# ═══════════════════════════════════════════════════

SKILL_TAXONOMY = {
    'Programming Languages': [
        'python', 'java', 'javascript', 'typescript', 'c++', 'c#', 'c',
        'ruby', 'php', 'swift', 'kotlin', 'go', 'rust', 'scala', 'r',
        'matlab', 'perl', 'dart', 'lua', 'haskell', 'elixir', 'clojure',
        'objective-c', 'assembly', 'vba', 'groovy', 'bash', 'powershell',
    ],
    'Web Frameworks': [
        'django', 'flask', 'fastapi', 'react', 'angular', 'vue.js', 'vue',
        'next.js', 'nuxt.js', 'express', 'node.js', 'spring boot', 'spring',
        'asp.net', '.net', 'rails', 'laravel', 'symfony', 'gatsby',
        'svelte', 'remix', 'ember.js',
    ],
    'Mobile': [
        'android', 'ios', 'flutter', 'react native', 'swift ui', 'swiftui',
        'jetpack compose', 'xamarin', 'ionic', 'cordova',
    ],
    'Data Science & AI': [
        'machine learning', 'deep learning', 'data science', 'nlp',
        'computer vision', 'tensorflow', 'pytorch', 'keras', 'scikit-learn',
        'pandas', 'numpy', 'scipy', 'opencv', 'hugging face', 'transformers',
        'llm', 'generative ai', 'neural networks', 'reinforcement learning',
        'xgboost', 'lightgbm', 'catboost', 'spark mllib',
    ],
    'Cloud & DevOps': [
        'aws', 'azure', 'gcp', 'google cloud', 'docker', 'kubernetes',
        'terraform', 'ansible', 'jenkins', 'github actions', 'ci/cd',
        'devops', 'cloudformation', 'helm', 'argocd', 'prometheus',
        'grafana', 'datadog', 'new relic', 'nginx', 'apache',
    ],
    'Databases': [
        'sql', 'mysql', 'postgresql', 'mongodb', 'redis', 'elasticsearch',
        'cassandra', 'dynamodb', 'firebase', 'supabase', 'sqlite',
        'oracle', 'mssql', 'neo4j', 'couchdb', 'influxdb',
    ],
    'Data Engineering': [
        'kafka', 'spark', 'hadoop', 'airflow', 'dbt', 'snowflake',
        'bigquery', 'redshift', 'databricks', 'flink', 'nifi',
        'etl', 'data pipeline', 'data warehouse',
    ],
    'Tools & Platforms': [
        'git', 'github', 'gitlab', 'bitbucket', 'jira', 'confluence',
        'slack', 'postman', 'swagger', 'figma', 'adobe xd', 'sketch',
        'photoshop', 'illustrator', 'blender', 'unity', 'unreal engine',
        'tableau', 'power bi', 'looker', 'excel', 'sas',
    ],
    'Methodologies': [
        'agile', 'scrum', 'kanban', 'waterfall', 'tdd', 'bdd',
        'microservices', 'rest api', 'graphql', 'grpc', 'soap',
        'event-driven', 'serverless', 'oauth', 'jwt',
    ],
    'Soft Skills': [
        'leadership', 'communication', 'teamwork', 'problem solving',
        'critical thinking', 'project management', 'mentoring',
        'public speaking', 'negotiation', 'time management',
    ],
}

ALL_SKILLS = []
for category, skills in SKILL_TAXONOMY.items():
    ALL_SKILLS.extend(skills)


def extract_skills_from_text(text):
    """Extract skills using the expanded taxonomy."""
    found_skills = []
    text_lower = text.lower()
    for skill in ALL_SKILLS:
        pattern = r'\b' + re.escape(skill) + r'\b'
        if re.search(pattern, text_lower):
            # Proper capitalization
            if len(skill) <= 3:
                found_skills.append(skill.upper())
            else:
                found_skills.append(skill.title())
    return list(set(found_skills))


def extract_experience_years(text):
    """Extract years of experience from text."""
    patterns = [
        r'(\d+)\+?\s*years?\s*(?:of\s*)?experience',
        r'(\d+)\+?\s*yrs?\s*exp',
        r'experience\s*[:\-]?\s*(\d+)\s*years?',
    ]
    max_years = 0
    for pattern in patterns:
        matches = re.findall(pattern, text.lower())
        for match in matches:
            try:
                years = int(match)
                if 0 < years < 40 and years > max_years:
                    max_years = years
            except (ValueError, TypeError):
                pass
    return max_years


def extract_degree(text, edu_text=""):
    """Extract the highest degree from education section or full text with strict word boundaries."""
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


# ═══════════════════════════════════════════════════
# MATCHING & SCORING ALGORITHMS
# ═══════════════════════════════════════════════════

def get_cosine_similarity(vec1, vec2):
    """Compute cosine similarity between two Counter vectors."""
    intersection = set(vec1.keys()) & set(vec2.keys())
    numerator = sum(vec1[x] * vec2[x] for x in intersection)
    sum1 = sum(vec1[x] ** 2 for x in vec1)
    sum2 = sum(vec2[x] ** 2 for x in vec2)
    denominator = math.sqrt(sum1) * math.sqrt(sum2)
    return float(numerator) / denominator if denominator else 0.0


def calculate_match_score(candidate_skills, required_skills):
    """Calculate match score between candidate and job skills."""
    if not required_skills or not candidate_skills:
        return 0.0
    
    if isinstance(required_skills, str):
        required_skills = [s.strip().lower() for s in required_skills.split(',')]
    if isinstance(candidate_skills, str):
        candidate_skills = [s.strip().lower() for s in candidate_skills.split(',')]
    if isinstance(required_skills, list):
        required_skills = [s.lower() if isinstance(s, str) else str(s).lower() for s in required_skills]
    if isinstance(candidate_skills, list):
        candidate_skills = [s.lower() if isinstance(s, str) else str(s).lower() for s in candidate_skills]
        
    vec1 = Counter(candidate_skills)
    vec2 = Counter(required_skills)
    
    return round(get_cosine_similarity(vec1, vec2) * 100, 2)


def verify_cv_authenticity(text):
    """Verify if a document looks like a legitimate CV."""
    if not text or len(text) < 150:
        return False, "Document too short to be a valid CV."

    text_lower = text.lower()
    markers = [
        'education', 'experience', 'skills', 'projects',
        'contact', 'summary', 'profile', 'work history',
        'university', 'degree', 'employment', 'objective',
        'certification', 'achievement', 'reference', 'volunteer',
    ]
    
    found_markers = [m for m in markers if re.search(r'\b' + m + r'\b', text_lower)]
    score = len(found_markers)
    is_authentic = score >= 3
    
    if is_authentic:
        message = f"CV authenticated successfully. ({score} professional markers detected)"
    else:
        message = f"Warning: This document may not be a standard CV. Only {score} marker(s) found."
    
    return is_authentic, message


def generate_cover_letter_with_gemini(job, candidate_profile):
    """Generate a personalized cover letter using Gemini API."""
    if not GEMINI_AVAILABLE or not GEMINI_API_KEY:
        return _generate_template_cover_letter(job, candidate_profile)
    
    try:
        model = genai.GenerativeModel('gemini-2.0-flash')
        
        skills = candidate_profile.extracted_skills_json if isinstance(candidate_profile.extracted_skills_json, list) else []
        job_skills = job.required_skills_json if isinstance(job.required_skills_json, list) else []
        
        prompt = f"""Write a professional cover letter for a job application.

Candidate:
- Name: {candidate_profile.user.full_name or candidate_profile.user.username}
- Skills: {', '.join(skills)}
- Experience: {candidate_profile.total_experience} years
- Degree: {candidate_profile.degree_extracted}
- Bio: {candidate_profile.bio}

Job:
- Title: {job.title}
- Company: {job.company}
- Required Skills: {', '.join(job_skills)}
- Description: {job.description[:500]}

Write a compelling, professional cover letter (250-350 words).
Do NOT use placeholders like [Your Name]. Use the actual data provided.
Be specific about how the candidate's skills match the job requirements."""
        
        response = model.generate_content(prompt)
        return response.text.strip()
        
    except Exception as e:
        print(f"Gemini cover letter error: {e}")
        return _generate_template_cover_letter(job, candidate_profile)


def _generate_template_cover_letter(job, candidate_profile):
    """Fallback template-based cover letter generation."""
    name = candidate_profile.user.full_name or candidate_profile.user.username
    skills = candidate_profile.extracted_skills_json if isinstance(candidate_profile.extracted_skills_json, list) else []
    job_skills = job.required_skills_json if isinstance(job.required_skills_json, list) else []
    matching = set(s.lower() for s in skills) & set(s.lower() for s in job_skills)
    
    return f"""Dear Hiring Manager,

I am writing to express my strong interest in the {job.title} position at {job.company}. With {candidate_profile.total_experience} years of professional experience and a {candidate_profile.degree_extracted} degree, I am confident in my ability to contribute meaningfully to your team.

My technical skill set includes {', '.join(skills[:8])}{'...' if len(skills) > 8 else ''}, which aligns well with your requirements. {'I am particularly strong in ' + ', '.join(list(matching)[:5]) + ', which are directly relevant to this role.' if matching else ''}

I would welcome the opportunity to discuss how my experience and skills can benefit {job.company}. Thank you for considering my application.

Best regards,
{name}"""
