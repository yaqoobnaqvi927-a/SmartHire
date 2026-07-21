from rest_framework import viewsets, permissions, decorators, status, serializers
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.pagination import PageNumberPagination
from .models import JobPosting, Application
from .serializers import JobPostingSerializer, ApplicationSerializer
from .search_engine import search_jobs
from cv_bank.services import generate_cover_letter_with_gemini
from jobs.services import calculate_match_score, analyze_skill_gap


class StandardPagination(PageNumberPagination):
    page_size = 20
    page_size_query_param = 'page_size'
    max_page_size = 100


class JobPostingViewSet(viewsets.ModelViewSet):
    serializer_class = JobPostingSerializer
    permission_classes = [permissions.IsAuthenticated]
    pagination_class = StandardPagination

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'recruiter_profile'):
            return JobPosting.objects.filter(recruiter=user.recruiter_profile).order_by('-created_at')
        return JobPosting.objects.filter(status='active').order_by('-created_at')

    def list(self, request, *args, **kwargs):
        """Enhanced job listing with TF-IDF search and ranking."""
        queryset = self.get_queryset()
        
        # Search parameters
        skills = request.query_params.get('skills', '')
        job_type = request.query_params.get('type', '')
        location = request.query_params.get('location', '')
        try:
            min_exp = int(request.query_params.get('experience', 0) or 0)
        except ValueError:
            min_exp = 0
        query = request.query_params.get('q', '')
        
        # Get candidate skills for personalized matching
        candidate_skills = None
        if hasattr(request.user, 'candidate_profile'):
            profile = request.user.candidate_profile
            if profile.extracted_skills_json:
                candidate_skills = [str(s) for s in profile.extracted_skills_json] if isinstance(profile.extracted_skills_json, list) else []
        
        # Use the search engine
        results = search_jobs(
            queryset=queryset,
            query_text=query,
            skills_filter=skills,
            job_type=job_type,
            min_experience=min_exp,
            location=location,
            candidate_skills=candidate_skills
        )
        
        # Serialize results
        data = []
        for result in results:
            job = result['job']
            serialized = JobPostingSerializer(job).data
            serialized['match_percentage'] = result['match_percentage']
            data.append(serialized)
        
        # Paginate manually
        page = self.paginate_queryset(data)
        if page is not None:
            return self.get_paginated_response(page)
        return Response(data)

    def perform_create(self, serializer):
        """Auto-populate recruiter and company from profile."""
        user = self.request.user
        if not hasattr(user, 'recruiter_profile'):
            raise permissions.exceptions.PermissionDenied("Only recruiters can post jobs")
        
        recruiter = user.recruiter_profile
        company = serializer.validated_data.get('company', '') or recruiter.company_name
        serializer.save(recruiter=recruiter, company=company)

    def update(self, request, *args, **kwargs):
        job = self.get_object()
        if not hasattr(request.user, 'recruiter_profile') or job.recruiter != request.user.recruiter_profile:
            raise permissions.exceptions.PermissionDenied("You can only edit your own job postings.")
        return super().update(request, *args, **kwargs)

    def partial_update(self, request, *args, **kwargs):
        job = self.get_object()
        if not hasattr(request.user, 'recruiter_profile') or job.recruiter != request.user.recruiter_profile:
            raise permissions.exceptions.PermissionDenied("You can only edit your own job postings.")
        return super().partial_update(request, *args, **kwargs)

    def destroy(self, request, *args, **kwargs):
        job = self.get_object()
        if not hasattr(request.user, 'recruiter_profile') or job.recruiter != request.user.recruiter_profile:
            raise permissions.exceptions.PermissionDenied("You can only delete your own job postings.")
        return super().destroy(request, *args, **kwargs)

    @action(detail=True, methods=['get'])
    def generate_cover_letter(self, request, pk=None):
        """Generate AI cover letter for a specific job."""
        job = self.get_object()
        
        if not hasattr(request.user, 'candidate_profile'):
            return Response({'error': 'Only candidates can generate cover letters'}, 
                          status=status.HTTP_403_FORBIDDEN)
        
        profile = request.user.candidate_profile
        try:
            letter = generate_cover_letter_with_gemini(job, profile)
        except Exception as e:
            return Response({'error': 'AI Generation failed. Please try again later.'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response({'cover_letter': letter})

    @action(detail=True, methods=['patch'])
    def toggle_status(self, request, pk=None):
        """Toggle job between active and paused."""
        job = self.get_object()
        if not hasattr(request.user, 'recruiter_profile') or job.recruiter != request.user.recruiter_profile:
            return Response({'error': 'Not authorized'}, status=status.HTTP_403_FORBIDDEN)
        
        job.status = 'paused' if job.status == 'active' else 'active'
        job.save()
        return Response(JobPostingSerializer(job).data)

    @action(detail=False, methods=['get'])
    def my_postings(self, request):
        """Get recruiter's own job postings with real-time counts."""
        if not hasattr(request.user, 'recruiter_profile'):
            return Response([], status=status.HTTP_200_OK)
        
        from django.db.models import Count, Q
        jobs = JobPosting.objects.filter(
            recruiter=request.user.recruiter_profile
        ).annotate(
            applicant_count_annotated=Count('applications'),
            ai_screened_count_annotated=Count('applications', filter=Q(applications__ai_match_score__gte=70))
        ).order_by('-created_at')
        
        data = []
        for job in jobs:
            serialized = JobPostingSerializer(job).data
            # Real-time counts
            serialized['applicant_count'] = job.applicant_count_annotated
            serialized['ai_screened_count'] = job.ai_screened_count_annotated
            data.append(serialized)
        
        return Response(data)

    @action(detail=False, methods=['get'])
    def top_match(self, request):
        """Get the top matched job for the current candidate (Match of the Day)."""
        if not hasattr(request.user, 'candidate_profile'):
            return Response({'error': 'No candidate profile'}, status=status.HTTP_404_NOT_FOUND)
        
        profile = request.user.candidate_profile
        candidate_skills = [str(s) for s in profile.extracted_skills_json] if isinstance(profile.extracted_skills_json, list) else []
        
        if not candidate_skills:
            # Return newest job if no skills parsed yet
            job = JobPosting.objects.filter(status='active').order_by('-created_at').first()
            if job:
                data = JobPostingSerializer(job).data
                data['match_percentage'] = 50.0
                return Response(data)
            return Response({}, status=status.HTTP_404_NOT_FOUND)
        
        results = search_jobs(
            queryset=JobPosting.objects.filter(status='active'),
            candidate_skills=candidate_skills
        )
        
        if results:
            top = results[0]
            data = JobPostingSerializer(top['job']).data
            data['match_percentage'] = top['match_percentage']
            return Response(data)
        
        return Response({}, status=status.HTTP_404_NOT_FOUND)

    @action(detail=False, methods=['get'])
    def stats(self, request):
        """Dashboard stats for both roles."""
        user = request.user
        
        if hasattr(user, 'candidate_profile'):
            profile = user.candidate_profile
            apps_sent = profile.applications.count()
            return Response({
                'apps_sent': apps_sent,
                'profile_views': profile.profile_views_count,
                'profile_completeness': profile.profile_completeness,
                'active_jobs_count': JobPosting.objects.filter(status='active').count(),
            })
        
        if hasattr(user, 'recruiter_profile'):
            recruiter = user.recruiter_profile
            total_jobs = recruiter.jobs.count()
            active_jobs = recruiter.jobs.filter(status='active').count()
            total_applications = Application.objects.filter(job__recruiter=recruiter).count()
            return Response({
                'total_jobs': total_jobs,
                'active_jobs': active_jobs,
                'total_applications': total_applications,
                'screened_candidates': Application.objects.filter(
                    job__recruiter=recruiter, ai_match_score__gte=70
                ).count(),
            })
        
        return Response({})

    @action(detail=False, methods=['get'])
    def search(self, request):
        """Keyword search endpoint /api/jobs/search/?q={keyword}"""
        query = request.query_params.get('q', '')
        from django.db.models import Q

        jobs = JobPosting.objects.filter(status='active').order_by('-created_at')
        if query:
            jobs = jobs.filter(
                Q(title__icontains=query) |
                Q(description__icontains=query) |
                Q(company__icontains=query)
            )

        # Serialize results
        data = JobPostingSerializer(jobs, many=True).data
        return Response(data)


class ApplicationViewSet(viewsets.ModelViewSet):
    serializer_class = ApplicationSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        if hasattr(user, 'candidate_profile'):
            return Application.objects.filter(candidate=user.candidate_profile).select_related('job', 'candidate__user')
        if hasattr(user, 'recruiter_profile'):
            return Application.objects.filter(job__recruiter=user.recruiter_profile).select_related('job', 'candidate__user')
        return Application.objects.none()

    def perform_create(self, serializer):
        user = self.request.user
        
        if hasattr(user, 'candidate_profile'):
            candidate = user.candidate_profile
        elif hasattr(user, 'recruiter_profile'):
            candidate = serializer.validated_data.get('candidate')
            if not candidate:
                raise serializers.ValidationError("Candidate ID required for recruiter applications")
        else:
            raise permissions.exceptions.PermissionDenied("Invalid role")
            
        job = serializer.validated_data['job']
        
        # Check for duplicate
        if Application.objects.filter(job=job, candidate=candidate).exists():
            raise serializers.ValidationError({"detail": "Already applied to this job"})
        
        # Calculate AI match score
        match_score = 0.0
        skill_gaps = []
        if candidate.extracted_skills_json:
            skills_str = " ".join(candidate.extracted_skills_json) if isinstance(candidate.extracted_skills_json, list) else str(candidate.extracted_skills_json)
            job_skills_str = " ".join(job.required_skills_json) if isinstance(job.required_skills_json, list) else str(job.required_skills_json)
            match_score = calculate_match_score(skills_str, job_skills_str)
            
            # Skill gap analysis
            skill_gaps = analyze_skill_gap(candidate.extracted_skills_json, job.required_skills_json)
            
        serializer.save(candidate=candidate, ai_match_score=match_score, skill_gap_analysis=skill_gaps)

    def partial_update(self, request, *args, **kwargs):
        """Allow status updates (ATS pipeline moves)."""
        instance = self.get_object()
        
        if not hasattr(request.user, 'recruiter_profile') or instance.job.recruiter != request.user.recruiter_profile:
            raise permissions.exceptions.PermissionDenied("You can only update applications for your own job postings.")

        new_status = request.data.get('status') or request.data.get('ats_status')
        
        if new_status:
            instance.ats_status = new_status
            instance.save()
            return Response(ApplicationSerializer(instance).data)
        
        return super().partial_update(request, *args, **kwargs)

