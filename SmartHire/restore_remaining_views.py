import os

files = {
    r"e:\FYP\SmartHire\users\views.py": """from rest_framework import generics, permissions, views, status
from rest_framework.response import Response
from django.contrib.auth import get_user_model
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
import json
import urllib.request
import urllib.parse
from .serializers import RegisterSerializer, UserSerializer, CandidateProfileSerializer, RecruiterProfileSerializer
from .models import CandidateProfile, RecruiterProfile

User = get_user_model()

class RegisterView(generics.CreateAPIView):
    queryset = User.objects.all()
    permission_classes = (permissions.AllowAny,)
    serializer_class = RegisterSerializer

class CustomTokenObtainPairSerializer(TokenObtainPairSerializer):
    def validate(self, attrs):
        data = super().validate(attrs)
        data['role'] = self.user.role_type
        if self.user.role_type == 'recruiter' and hasattr(self.user, 'recruiter_profile'):
            data['setup_complete'] = bool(self.user.recruiter_profile.company_name)
        elif self.user.role_type == 'student' and hasattr(self.user, 'candidate_profile'):
            data['setup_complete'] = bool(self.user.candidate_profile.extracted_skills_json)
        else:
            data['setup_complete'] = False
        return data

class CustomTokenObtainPairView(TokenObtainPairView):
    serializer_class = CustomTokenObtainPairSerializer

class ProfileView(generics.RetrieveUpdateAPIView):
    permission_classes = (permissions.IsAuthenticated,)

    def get_object(self):
        return self.request.user

    def get_serializer_class(self):
        return UserSerializer

    def get(self, request, *args, **kwargs):
        user = self.get_object()
        data = UserSerializer(user).data
        if user.role_type == 'student' and hasattr(user, 'candidate_profile'):
            data['profile'] = CandidateProfileSerializer(user.candidate_profile).data
        elif user.role_type == 'recruiter' and hasattr(user, 'recruiter_profile'):
            data['profile'] = RecruiterProfileSerializer(user.recruiter_profile).data
        return Response(data)

class ProfileSetupView(views.APIView):
    permission_classes = (permissions.IsAuthenticated,)

    def put(self, request, *args, **kwargs):
        user = request.user
        if user.role_type == 'student':
            profile = user.candidate_profile
            # Expected inbound: university, degree, skills
            profile.degree_extracted = request.data.get('degree', profile.degree_extracted)
            if 'skills' in request.data:
                profile.extracted_skills_json = [s.strip() for s in request.data['skills'].split(',')]
            profile.save()
            return Response({'status': 'success', 'message': 'Student Profile properly configured.'})
        elif user.role_type == 'recruiter':
            profile = user.recruiter_profile
            profile.company_name = request.data.get('company_name', profile.company_name)
            profile.company_size = request.data.get('company_size', profile.company_size)
            profile.industry = request.data.get('industry', profile.industry)
            profile.save()
            return Response({'status': 'success', 'message': 'Recruiter Account successfully wired.'})
        return Response({'error': 'Role configuration error'}, status=status.HTTP_400_BAD_REQUEST)

class SearchCandidatesView(generics.ListAPIView):
    serializer_class = CandidateProfileSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        queryset = CandidateProfile.objects.all()
        skills = self.request.query_params.get('skills', None)
        experience = self.request.query_params.get('experience', None)
        degree = self.request.query_params.get('degree', None)

        from django.db.models import Q
        if skills:
            skill_list = skills.split(',')
            query = Q()
            for skill in skill_list:
                query &= Q(search_keywords_index__icontains=skill.strip().lower())
            queryset = queryset.filter(query)

        if experience:
            try:
                queryset = queryset.filter(total_experience__gte=int(experience))
            except ValueError:
                pass

        if degree:
            queryset = queryset.filter(degree_extracted__icontains=degree)

        return queryset

    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        req_skills = request.query_params.get('skills', '')
        min_exp = int(request.query_params.get('experience', 0) or 0)
        
        serializer = self.get_serializer(queryset, many=True)
        data = [dict(item) for item in serializer.data]
        
        for item in data:
            candidate_skills = " ".join(item.get('extracted_skills_json', [])) if isinstance(item.get('extracted_skills_json', []), list) else str(item.get('extracted_skills_json', ''))
            score = 100.0
            if req_skills:
                from jobs.services import calculate_bidirectional_match
                score = calculate_bidirectional_match(req_skills, candidate_skills, exp_required=min_exp, exp_actual=item.get('total_experience', 0))
            item['match_percentage'] = score
            
        data.sort(key=lambda x: x.get('match_percentage', 0), reverse=True)
        return Response(data)

class GoogleLoginView(views.APIView):
    permission_classes = (permissions.AllowAny,)

    def post(self, request, *args, **kwargs):
        id_token = request.data.get('id_token')
        role = request.data.get('role_type', 'student')
        if not id_token:
            return Response({'error': 'id_token is required'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            url = f"https://oauth2.googleapis.com/tokeninfo?id_token={id_token}"
            with urllib.request.urlopen(url) as response:
                token_info = json.loads(response.read())

            if 'error' in token_info:
                return Response({'error': 'Invalid token'}, status=status.HTTP_400_BAD_REQUEST)

            email = token_info.get('email')
            if not email:
                return Response({'error': 'No email from Google'}, status=status.HTTP_400_BAD_REQUEST)

            user, created = User.objects.get_or_create(email=email, defaults={
                'username': email.split('@')[0],
                'role_type': role,
                'full_name': token_info.get('name', '')
            })
            
            if created:
                user.set_unusable_password()
                user.save()
                if user.role_type == 'student':
                    CandidateProfile.objects.create(user=user)
                elif user.role_type == 'recruiter':
                    RecruiterProfile.objects.create(user=user)

            refresh = RefreshToken.for_user(user)
            refresh['role'] = user.role_type
            if user.role_type == 'recruiter' and hasattr(user, 'recruiter_profile'):
                refresh['setup_complete'] = bool(user.recruiter_profile.company_name)
            elif user.role_type == 'student' and hasattr(user, 'candidate_profile'):
                refresh['setup_complete'] = bool(user.candidate_profile.extracted_skills_json)

            return Response({
                'refresh': str(refresh),
                'access': str(refresh.access_token),
            })
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
""",
        
    r"e:\FYP\SmartHire\cv_bank\views.py": """from rest_framework import viewsets, permissions, status
from rest_framework.response import Response
from .models import CV
from .serializers import CVSerializer
from .services import extract_text_from_pdf, extract_skills_from_text, extract_experience_years, extract_degree, verify_cv_authenticity
from users.models import CandidateProfile

class CVViewSet(viewsets.ModelViewSet):
    serializer_class = CVSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return CV.objects.filter(user=self.request.user)

    def create(self, request, *args, **kwargs):
        file_obj = request.FILES.get('file')
        if not file_obj:
            return Response({'error': 'No file uploaded'}, status=status.HTTP_400_BAD_REQUEST)

        text = extract_text_from_pdf(file_obj)
        is_authentic, auth_message = verify_cv_authenticity(text)
        
        skills = extract_skills_from_text(text)
        exp_years = extract_experience_years(text)
        degree = extract_degree(text)

        cv = CV.objects.create(
            user=request.user,
            file=file_obj,
            extracted_text=text,
            skills_extracted=",".join(skills),
            is_primary=True
        )

        try:
            profile = CandidateProfile.objects.get(user=request.user)
            profile.extracted_skills_json = skills
            profile.total_experience = exp_years
            profile.degree_extracted = degree
            profile.cv_file_path = cv.file
            profile.save()
        except CandidateProfile.DoesNotExist:
            pass

        serializer = self.get_serializer(cv)
        return Response({
            'cv': serializer.data,
            'extracted': {
                'skills': skills,
                'total_experience': exp_years,
                'degree_extracted': degree,
                'is_authentic': is_authentic,
                'auth_message': auth_message
            }
        }, status=status.HTTP_201_CREATED)
""",

    r"e:\FYP\SmartHire\interviews\views.py": """from rest_framework import viewsets, permissions, status, decorators
from rest_framework.response import Response
from .models import ScheduledInterview
from .serializers import ScheduledInterviewSerializer
from communications.models import Notification

class ScheduledInterviewViewSet(viewsets.ModelViewSet):
    serializer_class = ScheduledInterviewSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'recruiter_profile'):
            return ScheduledInterview.objects.filter(application__job__recruiter=user.recruiter_profile)
        if hasattr(user, 'candidate_profile'):
            return ScheduledInterview.objects.filter(application__candidate=user.candidate_profile)
        return ScheduledInterview.objects.none()

    @decorators.action(detail=True, methods=['post'])
    def confirm(self, request, pk=None):
        interview = self.get_object()
        interview.status = 'confirmed'
        interview.save()
        
        Notification.objects.create(
            user=interview.application.job.recruiter.user,
            title="Interview Confirmed",
            message=f"{interview.application.candidate.user.full_name} confirmed the interview for {interview.application.job.title}."
        )
        return Response({"status": "confirmed"})

    @decorators.action(detail=True, methods=['post'])
    def decline(self, request, pk=None):
        interview = self.get_object()
        interview.status = 'declined'
        interview.save()
        
        Notification.objects.create(
            user=interview.application.job.recruiter.user,
            title="Interview Declined",
            message=f"{interview.application.candidate.user.full_name} declined the interview for {interview.application.job.title}."
        )
        return Response({"status": "declined"})
"""
}

for filepath, content in files.items():
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Updated {filepath}")
