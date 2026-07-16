from rest_framework import viewsets, permissions, status
from rest_framework.response import Response
from .models import CV
from .serializers import CVSerializer
from .services import (
    extract_text_from_pdf, parse_cv_with_gemini,
    verify_cv_authenticity, extract_skills_from_text,
    extract_experience_years, extract_degree
)
from users.models import CandidateProfile


class CVViewSet(viewsets.ModelViewSet):
    serializer_class = CVSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return CV.objects.filter(user=self.request.user).order_by('-uploaded_at')

    def create(self, request, *args, **kwargs):
        file_obj = request.FILES.get('file')
        if not file_obj:
            return Response({'error': 'No file uploaded'}, status=status.HTTP_400_BAD_REQUEST)

        # Step 1: Extract raw text
        text = extract_text_from_pdf(file_obj)
        if not text.strip():
            return Response({'error': 'Could not extract text from PDF. The file may be image-based.'}, 
                          status=status.HTTP_400_BAD_REQUEST)

        # Step 2: Verify authenticity
        is_authentic, auth_message = verify_cv_authenticity(text)

        # Step 3: Parse with Gemini AI (falls back to regex)
        parsed = parse_cv_with_gemini(text)

        # Step 4: Save CV record
        cv = CV.objects.create(
            user=request.user,
            file=file_obj,
            extracted_text=text,
            skills_extracted=",".join(parsed.get('skills', [])),
            parsed_experience=parsed.get('total_experience_years', 0),
            parsed_education=parsed.get('education', []),
            is_primary=True
        )

        # Step 5: Update CandidateProfile with parsed data
        try:
            profile, created = CandidateProfile.objects.get_or_create(user=request.user)
            profile.extracted_skills_json = parsed.get('skills', [])
            profile.total_experience = parsed.get('total_experience_years', 0)
            profile.degree_extracted = parsed.get('degree', '')
            profile.bio = parsed.get('bio', '')
            profile.location = parsed.get('location', '')
            profile.education_json = parsed.get('education', [])
            profile.work_experience_json = parsed.get('work_experience', [])
            profile.certifications_json = parsed.get('certifications', [])
            profile.cv_file_path = cv.file
            
            # Update URLs if parsed
            if parsed.get('linkedin_url'):
                profile.linkedin_url = parsed['linkedin_url']
            if parsed.get('github_url'):
                profile.github_url = parsed['github_url']
            if parsed.get('portfolio_url'):
                profile.portfolio_url = parsed['portfolio_url']
            
            # Update user's full name if parsed and not already set
            if parsed.get('full_name') and not request.user.full_name:
                request.user.full_name = parsed['full_name']
                request.user.save(update_fields=['full_name'])
            
            profile.save()  # This triggers profile_completeness calculation
            
        except Exception as e:
            print(f"Error updating profile: {e}")

        serializer = self.get_serializer(cv)
        return Response({
            'cv': serializer.data,
            'extracted': {
                'skills': parsed.get('skills', []),
                'experience_years': parsed.get('total_experience_years', 0),
                'degree': parsed.get('degree', ''),
                'bio': parsed.get('bio', ''),
                'education': parsed.get('education', []),
                'work_experience': parsed.get('work_experience', []),
                'certifications': parsed.get('certifications', []),
                'location': parsed.get('location', ''),
                'is_authentic': is_authentic,
                'auth_message': auth_message,
                'profile_completeness': profile.profile_completeness if 'profile' in dir() else 0,
            }
        }, status=status.HTTP_201_CREATED)
