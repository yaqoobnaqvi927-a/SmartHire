"""
SmartHire Intelligent Search Engine
TF-IDF weighted search with multi-factor ranking for jobs and candidates.
"""
import math
import json
import re
from collections import Counter
from django.db.models import Q, F
from django.utils import timezone
from datetime import timedelta


def _tokenize(text):
    """Tokenize and normalize text into word counts."""
    if not text:
        return Counter()
    text = text.lower().strip()
    words = re.findall(r'\b\w+\b', text)
    # Remove common stop words
    stop_words = {'the', 'a', 'an', 'is', 'are', 'was', 'were', 'be', 'been',
                  'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for', 'of', 'with',
                  'by', 'from', 'as', 'into', 'through', 'during', 'before', 'after',
                  'this', 'that', 'these', 'those', 'it', 'its', 'we', 'our', 'you', 'your'}
    words = [w for w in words if w not in stop_words and len(w) > 1]
    return Counter(words)


def _cosine_similarity(vec1, vec2):
    """Compute cosine similarity between two Counter vectors."""
    intersection = set(vec1.keys()) & set(vec2.keys())
    numerator = sum(vec1[x] * vec2[x] for x in intersection)
    sum1 = sum(v ** 2 for v in vec1.values())
    sum2 = sum(v ** 2 for v in vec2.values())
    denominator = math.sqrt(sum1) * math.sqrt(sum2)
    return float(numerator) / denominator if denominator else 0.0


def _skill_overlap_score(query_skills, target_skills):
    """Calculate what % of query skills are found in target skills."""
    if not query_skills:
        return 1.0  # No filter = 100% match
    
    query_set = {s.strip().lower() for s in query_skills if s.strip()}
    target_set = {s.strip().lower() for s in target_skills if s.strip()}
    
    if not query_set:
        return 1.0
    
    overlap = query_set & target_set
    return len(overlap) / len(query_set)


def _recency_boost(created_at):
    """Give a boost to recently created items (0.0 to 0.15)."""
    if not created_at:
        return 0.0
    age = timezone.now() - created_at
    if age < timedelta(days=1):
        return 0.15
    elif age < timedelta(days=7):
        return 0.10
    elif age < timedelta(days=30):
        return 0.05
    return 0.0


def search_jobs(queryset, query_text='', skills_filter='', job_type='', 
                min_experience=0, location='', candidate_skills=None):
    """
    Search and rank jobs using TF-IDF + multi-factor scoring.
    
    Scoring formula:
      score = (0.4 * text_similarity) + (0.35 * skill_overlap) + 
              (0.10 * experience_match) + (0.15 * recency_boost)
    
    Returns list of dicts with match_percentage added.
    """
    # Step 1: Filter queryset
    if skills_filter:
        skill_list = [s.strip() for s in skills_filter.split(',') if s.strip()]
        query = Q()
        for skill in skill_list:
            query &= Q(search_keywords_index__icontains=skill.lower())
        queryset = queryset.filter(query)
    
    if job_type:
        queryset = queryset.filter(job_type__icontains=job_type)
    
    if location:
        queryset = queryset.filter(
            Q(location__icontains=location) | Q(search_keywords_index__icontains=location.lower())
        )
    
    # Step 2: Score and rank
    results = []
    query_vec = _tokenize(f"{query_text} {skills_filter}")
    query_skills = [s.strip() for s in skills_filter.split(',') if s.strip()] if skills_filter else []
    
    for job in queryset:
        # Text similarity (TF-IDF proxy)
        job_text = job.search_keywords_index or ''
        job_vec = _tokenize(job_text)
        text_sim = _cosine_similarity(query_vec, job_vec) if query_text or skills_filter else 1.0
        
        # Skill overlap
        job_skills = job.required_skills_json if isinstance(job.required_skills_json, list) else []
        if candidate_skills:
            skill_score = _skill_overlap_score(candidate_skills, [str(s) for s in job_skills])
        elif query_skills:
            skill_score = _skill_overlap_score(query_skills, [str(s) for s in job_skills])
        else:
            skill_score = 1.0
        
        # Experience match
        exp_score = 1.0
        job_min_exp = job.min_experience or 0
        if min_experience > 0 and job_min_exp > 0:
            if min_experience >= job_min_exp:
                exp_score = 1.0
            else:
                exp_score = min_experience / job_min_exp
        
        # Recency
        recency = _recency_boost(job.created_at)
        
        # Final composite score
        final_score = (0.4 * text_sim) + (0.35 * skill_score) + (0.10 * exp_score) + (0.15 * recency)
        match_pct = round(min(final_score * 100, 99.9), 1)
        
        # If no search criteria, show all at base score
        if not query_text and not skills_filter and not candidate_skills:
            match_pct = 100.0
        
        results.append({
            'job': job,
            'match_percentage': match_pct,
        })
    
    # Sort by match percentage descending
    results.sort(key=lambda x: x['match_percentage'], reverse=True)
    return results


def search_candidates(queryset, query_skills='', min_experience=0, degree='',
                       required_skills_list=None):
    """
    Search and rank candidates using TF-IDF + multi-factor scoring.
    
    Scoring formula:
      score = (0.35 * skill_overlap) + (0.25 * text_similarity) + 
              (0.15 * experience_match) + (0.10 * profile_completeness) + 
              (0.15 * recency_boost)
    """
    # Step 1: Filter
    if query_skills:
        skill_list = [s.strip() for s in query_skills.split(',') if s.strip()]
        query = Q()
        for skill in skill_list:
            query &= Q(search_keywords_index__icontains=skill.lower())
        queryset = queryset.filter(query)
    
    if min_experience > 0:
        queryset = queryset.filter(total_experience__gte=min_experience)
    
    if degree:
        queryset = queryset.filter(degree_extracted__icontains=degree)
    
    # Only show searchable profiles
    queryset = queryset.filter(is_searchable=True)
    
    # Step 2: Score and rank
    results = []
    query_vec = _tokenize(query_skills)
    req_skills = required_skills_list or [s.strip() for s in query_skills.split(',') if s.strip()]
    
    for candidate in queryset:
        # Skill overlap
        cand_skills = candidate.extracted_skills_json if isinstance(candidate.extracted_skills_json, list) else []
        skill_score = _skill_overlap_score(req_skills, [str(s) for s in cand_skills])
        
        # Text similarity
        cand_vec = _tokenize(candidate.search_keywords_index or '')
        text_sim = _cosine_similarity(query_vec, cand_vec) if query_skills else 1.0
        
        # Experience
        exp_score = 1.0
        cand_total_exp = candidate.total_experience or 0
        if min_experience > 0 and cand_total_exp > 0:
            exp_score = min(cand_total_exp / max(min_experience, 1), 1.5) / 1.5
        
        # Profile completeness (normalized 0-1)
        completeness = (candidate.profile_completeness or 0) / 100.0
        
        # Recency
        recency = _recency_boost(candidate.last_active) if hasattr(candidate, 'last_active') and candidate.last_active else 0.0
        
        # Final score
        final_score = (0.35 * skill_score) + (0.25 * text_sim) + (0.15 * exp_score) + (0.10 * completeness) + (0.15 * recency)
        match_pct = round(min(final_score * 100, 99.9), 1)
        
        if not query_skills and not required_skills_list:
            match_pct = 100.0
        
        results.append({
            'candidate': candidate,
            'match_percentage': match_pct,
        })
    
    results.sort(key=lambda x: x['match_percentage'], reverse=True)
    return results
