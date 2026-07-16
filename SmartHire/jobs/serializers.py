from rest_framework import serializers
from .models import JobPosting, Application
from users.serializers import CandidateProfileSerializer

class JobPostingSerializer(serializers.ModelSerializer):
    match_percentage = serializers.FloatField(read_only=True, required=False, default=0.0)
    recruiter_company = serializers.CharField(source='recruiter.company_name', read_only=True, default='')
    
    class Meta:
        model = JobPosting
        fields = '__all__'
        read_only_fields = ('recruiter', 'search_keywords_index', 'vector_profile', 
                          'applicant_count', 'ai_screened_count', 'created_at', 'updated_at')

class ApplicationSerializer(serializers.ModelSerializer):
    candidate_details = CandidateProfileSerializer(source='candidate', read_only=True)
    job_details = JobPostingSerializer(source='job', read_only=True)
    
    class Meta:
        model = Application
        fields = '__all__'
        read_only_fields = ('candidate', 'ai_match_score', 'skill_gap_analysis')
