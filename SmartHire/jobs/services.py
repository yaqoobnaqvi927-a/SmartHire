import math
import json
from collections import Counter
import re

def get_cosine_similarity(vec1, vec2):
    intersection = set(vec1.keys()) & set(vec2.keys())
    numerator = sum([vec1[x] * vec2[x] for x in intersection])

    sum1 = sum([vec1[x] ** 2 for x in list(vec1.keys())])
    sum2 = sum([vec2[x] ** 2 for x in list(vec2.keys())])
    denominator = math.sqrt(sum1) * math.sqrt(sum2)

    if not denominator:
        return 0.0
    else:
        return float(numerator) / denominator

def _tokenize(text):
    text = text.lower()
    # Simple tokenization: extract words
    words = re.findall(r'\b\w+\b', text)
    return Counter(words)

def calculate_match_score(cv_skills_text, job_skills_text):
    """
    Calculates a cosine similarity score between the parsed CV skills
    and the job requirements. (Pure Python version avoiding sklearn)
    """
    if not cv_skills_text or not job_skills_text:
        return 0.0
    
    vec1 = _tokenize(cv_skills_text)
    vec2 = _tokenize(job_skills_text)
    
    similarity = get_cosine_similarity(vec1, vec2)
    return round(float(similarity) * 100, 2)

def calculate_bidirectional_match(target_skills, source_skills_json, exp_required=0, exp_actual=0):
    """
    Core AI Search Algorithm:
    Compares target skills (string) with source skills (json string or comma string)
    and applies a penalty if experience is lower than required.
    """
    if not target_skills:
        return 100.0 # No filter means 100% match by default
        
    try:
        if source_skills_json.startswith('['):
            source_text = " ".join(json.loads(source_skills_json))
        else:
            source_text = source_skills_json.replace(",", " ")
            
        vec1 = _tokenize(target_skills.replace(",", " "))
        vec2 = _tokenize(source_text)
        
        score = get_cosine_similarity(vec1, vec2) * 100
        
        # Experience Check
        if exp_actual < exp_required:
            score *= 0.8
            
        return round(min(float(score), 100.0), 2)
    except Exception as e:
        print("Error bidirectional match:", e)
        return 50.0

def analyze_skill_gap(candidate_skills, job_skills_required):
    """
    Performs set difference to output a list of missing critical skills.
    """
    if isinstance(candidate_skills, str):
        candidate_skills = [s.strip().lower() for s in candidate_skills.split(',') if s.strip()]
    if isinstance(job_skills_required, str):
        job_skills_required = [s.strip().lower() for s in job_skills_required.split(',') if s.strip()]
        
    candidate_set = set(candidate_skills)
    job_set = set(job_skills_required)
    
    missing = list(job_set - candidate_set)
    return missing
