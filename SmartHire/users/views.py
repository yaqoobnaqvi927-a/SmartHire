from rest_framework import generics, permissions, views, status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from django.contrib.auth import get_user_model
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
import json
import urllib.request
from .serializers import RegisterSerializer, UserSerializer, CandidateProfileSerializer, RecruiterProfileSerializer
from .models import CandidateProfile, RecruiterProfile
from jobs.search_engine import search_candidates

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
            profile = user.candidate_profile
            data['profile'] = CandidateProfileSerializer(profile).data
        elif user.role_type == 'recruiter' and hasattr(user, 'recruiter_profile'):
            data['profile'] = RecruiterProfileSerializer(user.recruiter_profile).data
        return Response(data)

    def patch(self, request, *args, **kwargs):
        """Allow partial profile updates."""
        user = self.get_object()
        if user.role_type == 'student' and hasattr(user, 'candidate_profile'):
            profile = user.candidate_profile
            for field in ['bio', 'location', 'github_url', 'linkedin_url', 'portfolio_url']:
                if field in request.data:
                    setattr(profile, field, request.data[field])
            if 'skills' in request.data:
                skills = request.data['skills']
                if isinstance(skills, str):
                    profile.extracted_skills_json = [s.strip() for s in skills.split(',') if s.strip()]
                elif isinstance(skills, list):
                    profile.extracted_skills_json = skills
            profile.save()
            return Response(CandidateProfileSerializer(profile).data)
        return Response({'error': 'Cannot update this profile type'}, status=status.HTTP_400_BAD_REQUEST)


class ProfileSetupView(views.APIView):
    permission_classes = (permissions.IsAuthenticated,)

    def put(self, request, *args, **kwargs):
        user = request.user
        if user.role_type == 'student':
            profile, _ = CandidateProfile.objects.get_or_create(user=user)
            profile.degree_extracted = request.data.get('degree', profile.degree_extracted)
            if 'skills' in request.data:
                skills = request.data['skills']
                if isinstance(skills, str):
                    profile.extracted_skills_json = [s.strip() for s in skills.split(',') if s.strip()]
                elif isinstance(skills, list):
                    profile.extracted_skills_json = skills
            if 'bio' in request.data:
                profile.bio = request.data['bio']
            if 'location' in request.data:
                profile.location = request.data['location']
            profile.save()
            return Response({'status': 'success', 'message': 'Profile configured successfully.'})
        elif user.role_type == 'recruiter':
            profile, _ = RecruiterProfile.objects.get_or_create(user=user, defaults={'company_name': ''})
            profile.company_name = request.data.get('company_name', profile.company_name)
            profile.company_size = request.data.get('company_size', profile.company_size)
            profile.industry = request.data.get('industry', profile.industry)
            profile.save()
            return Response({'status': 'success', 'message': 'Recruiter account configured.'})
        return Response({'error': 'Role configuration error'}, status=status.HTTP_400_BAD_REQUEST)


class SearchCandidatesView(generics.ListAPIView):
    """AI-powered candidate search with TF-IDF ranking."""
    serializer_class = CandidateProfileSerializer
    permission_classes = [permissions.IsAuthenticated]

    def list(self, request, *args, **kwargs):
        from rest_framework.exceptions import PermissionDenied
        if request.user.role_type != 'recruiter':
            raise PermissionDenied("Only recruiters can search candidates.")

        queryset = CandidateProfile.objects.all().select_related('user')
        
        skills = request.query_params.get('skills', '')
        try:
            experience = int(request.query_params.get('experience', 0) or 0)
        except ValueError:
            experience = 0
            
        degree = request.query_params.get('degree', '')
        
        # Use the search engine
        results = search_candidates(
            queryset=queryset,
            query_skills=skills,
            min_experience=experience,
            degree=degree
        )
        
        data = []
        for result in results:
            candidate = result['candidate']
            serialized = CandidateProfileSerializer(candidate).data
            serialized['match_percentage'] = result['match_percentage']
            data.append(serialized)
        
        return Response(data)


class CandidateProfileDetailView(generics.RetrieveAPIView):
    """Get full candidate profile (increments view count)."""
    serializer_class = CandidateProfileSerializer
    permission_classes = [permissions.IsAuthenticated]
    queryset = CandidateProfile.objects.all()

    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        # Increment view count if viewer is not the profile owner
        if request.user != instance.user:
            instance.profile_views_count += 1
            instance.save(update_fields=['profile_views_count'])
        serializer = self.get_serializer(instance)
        return Response(serializer.data)


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
                user.first_name = token_info.get('given_name', '')
                user.last_name = token_info.get('family_name', '')
                user.save()
                if user.role_type == 'student':
                    CandidateProfile.objects.create(user=user)
                elif user.role_type == 'recruiter':
                    RecruiterProfile.objects.create(user=user, company_name='')

            refresh = RefreshToken.for_user(user)
            
            setup_complete = False
            if user.role_type == 'recruiter' and hasattr(user, 'recruiter_profile'):
                setup_complete = bool(user.recruiter_profile.company_name)
            elif user.role_type == 'student' and hasattr(user, 'candidate_profile'):
                setup_complete = bool(user.candidate_profile.extracted_skills_json)

            return Response({
                'refresh': str(refresh),
                'access': str(refresh.access_token),
                'role': user.role_type,
                'setup_complete': setup_complete,
            })
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


# ---------------------------------------------------------------------------
# FCM Device Token Registration
# ---------------------------------------------------------------------------

@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def register_fcm_token(request):
    """
    POST /api/users/fcm-token/

    Register or update an FCM device token for the authenticated user.
    Called by the mobile app after it receives a new FCM registration token.

    Request body:
        fcm_token   (str, required) — the FCM token from the device.
        device_type (str, optional) — 'android' | 'ios' | 'web'. Default: 'android'.

    Response:
        200 OK — {'status': 'Token registered successfully', 'created': bool}
        400 Bad Request — {'error': 'fcm_token is required'}
    """
    token = request.data.get('fcm_token', '').strip()
    device_type = request.data.get('device_type', 'android').strip()

    if not token:
        return Response(
            {'error': 'fcm_token is required.'},
            status=status.HTTP_400_BAD_REQUEST,
        )

    # Validate device_type value
    allowed_device_types = {'android', 'ios', 'web'}
    if device_type not in allowed_device_types:
        device_type = 'android'

    from communications.models import DeviceToken

    obj, created = DeviceToken.objects.get_or_create(
        user=request.user,
        fcm_token=token,
        defaults={'device_type': device_type},
    )

    # If token already existed but device_type changed, update it
    if not created and obj.device_type != device_type:
        obj.device_type = device_type
        obj.save(update_fields=['device_type', 'updated_at'])

    return Response(
        {
            'status': 'Token registered successfully.',
            'created': created,
        },
        status=status.HTTP_200_OK,
    )
