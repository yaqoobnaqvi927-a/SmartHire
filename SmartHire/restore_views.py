import os

files = {
    r"e:\FYP\SmartHire\users\serializers.py": """from rest_framework import serializers
from .models import User, CandidateProfile, RecruiterProfile
from django.contrib.auth import authenticate

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'role_type', 'full_name')

class CandidateProfileSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    class Meta:
        model = CandidateProfile
        fields = '__all__'

class RecruiterProfileSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    class Meta:
        model = RecruiterProfile
        fields = '__all__'

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    role_type = serializers.CharField(write_only=True, required=False)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'password', 'role_type')

    def create(self, validated_data):
        role = validated_data.get('role_type', 'student')
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data.get('email', ''),
            password=validated_data['password'],
            role_type=role
        )
        if role == 'student':
            CandidateProfile.objects.create(user=user)
        elif role == 'recruiter':
            RecruiterProfile.objects.create(user=user)
        return user
""",

    r"e:\FYP\SmartHire\jobs\serializers.py": """from rest_framework import serializers
from .models import JobPosting, Application
from users.serializers import CandidateProfileSerializer

class JobPostingSerializer(serializers.ModelSerializer):
    match_percentage = serializers.FloatField(read_only=True, required=False)
    
    class Meta:
        model = JobPosting
        fields = '__all__'
        read_only_fields = ('recruiter', 'search_keywords_index', 'vector_profile')

class ApplicationSerializer(serializers.ModelSerializer):
    candidate_details = CandidateProfileSerializer(source='candidate', read_only=True)
    job_details = JobPostingSerializer(source='job', read_only=True)
    
    class Meta:
        model = Application
        fields = '__all__'
        read_only_fields = ('candidate', 'ai_match_score', 'skill_gap_analysis')
""",

    r"e:\FYP\SmartHire\communications\serializers.py": """from rest_framework import serializers
from .models import Notification, ChatThread, ChatMessage
from users.serializers import UserSerializer, CandidateProfileSerializer, RecruiterProfileSerializer

class NotificationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Notification
        fields = '__all__'

class ChatThreadSerializer(serializers.ModelSerializer):
    candidate_details = CandidateProfileSerializer(source='candidate', read_only=True)
    recruiter_details = RecruiterProfileSerializer(source='recruiter', read_only=True)
    class Meta:
        model = ChatThread
        fields = '__all__'

class ChatMessageSerializer(serializers.ModelSerializer):
    sender_details = UserSerializer(source='sender', read_only=True)
    class Meta:
        model = ChatMessage
        fields = '__all__'
""",

    r"e:\FYP\SmartHire\interviews\serializers.py": """from rest_framework import serializers
from .models import ScheduledInterview
from jobs.serializers import ApplicationSerializer

class ScheduledInterviewSerializer(serializers.ModelSerializer):
    application_details = ApplicationSerializer(source='application', read_only=True)
    
    class Meta:
        model = ScheduledInterview
        fields = '__all__'
""",

    r"e:\FYP\SmartHire\jobs\views.py": """from rest_framework import viewsets, permissions, decorators
from rest_framework.response import Response
from .models import JobPosting, Application
from .serializers import JobPostingSerializer, ApplicationSerializer
from .services import calculate_match_score

class JobPostingViewSet(viewsets.ModelViewSet):
    serializer_class = JobPostingSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        queryset = JobPosting.objects.filter(status='active').order_by('-created_at')
        if hasattr(user, 'recruiter_profile'):
            queryset = JobPosting.objects.filter(recruiter=user.recruiter_profile).order_by('-created_at')

        skills = self.request.query_params.get('skills', None)
        job_type = self.request.query_params.get('type', None)

        from django.db.models import Q
        if skills:
            skill_list = skills.split(',')
            query = Q()
            for skill in skill_list:
                query &= Q(search_keywords_index__icontains=skill.strip().lower())
            queryset = queryset.filter(query)

        if job_type:
            queryset = queryset.filter(job_type__icontains=job_type)

        return queryset

    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        skills = request.query_params.get('skills', '')
        exp = int(request.query_params.get('experience', 0) or 0)
        
        serializer = self.get_serializer(queryset, many=True)
        data = [dict(item) for item in serializer.data]
        
        for item in data:
            job_skills = " ".join(item.get('required_skills_json', [])) if isinstance(item.get('required_skills_json', []), list) else str(item.get('required_skills_json', ''))
            score = 100.0
            if skills:
                from .services import calculate_bidirectional_match
                score = calculate_bidirectional_match(skills, job_skills, exp_required=item.get('min_experience', 0), exp_actual=exp)
            item['match_percentage'] = score
            
        data.sort(key=lambda x: x['match_percentage'], reverse=True)
        return Response(data)

class ApplicationViewSet(viewsets.ModelViewSet):
    serializer_class = ApplicationSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'candidate_profile'):
            return Application.objects.filter(candidate=user.candidate_profile)
        if hasattr(user, 'recruiter_profile'):
            return Application.objects.filter(job__recruiter=user.recruiter_profile)
        return Application.objects.none()

    def perform_create(self, serializer):
        user = self.request.user
        if hasattr(user, 'recruiter_profile'):
            candidate = serializer.validated_data.get('candidate')
        elif hasattr(user, 'candidate_profile'):
            candidate = user.candidate_profile
        else:
            raise permissions.exceptions.PermissionDenied("Invalid")
            
        job = serializer.validated_data['job']
        match_score = 0.0
        
        if candidate.extracted_skills_json:
            skills_str = " ".join(candidate.extracted_skills_json) if isinstance(candidate.extracted_skills_json, list) else candidate.extracted_skills_json
            job_skills_str = " ".join(job.required_skills_json) if isinstance(job.required_skills_json, list) else job.required_skills_json
            match_score = calculate_match_score(skills_str, job_skills_str)
            
        serializer.save(candidate=candidate, ai_match_score=match_score)

""",
}

for filepath, content in files.items():
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Updated {filepath}")
