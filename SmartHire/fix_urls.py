import os

urls_to_fix = {
    r"e:\FYP\SmartHire\jobs\urls.py": """from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import JobPostingViewSet, ApplicationViewSet

router = DefaultRouter()
router.register(r'jobs', JobPostingViewSet, basename='jobposting')
router.register(r'applications', ApplicationViewSet, basename='application')

urlpatterns = [
    path('', include(router.urls)),
]
""",

    r"e:\FYP\SmartHire\users\urls.py": """from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import RegisterView, ProfileView, CustomTokenObtainPairView, SearchCandidatesView, ProfileSetupView, GoogleLoginView

urlpatterns = [
    path('register/', RegisterView.as_view(), name='auth_register'),
    path('login/', CustomTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('google-login/', GoogleLoginView.as_view(), name='google_login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('profile/', ProfileView.as_view(), name='profile'),
    path('profile/setup/', ProfileSetupView.as_view(), name='profile_setup'),
    path('search/candidates/', SearchCandidatesView.as_view(), name='search_candidates'),
]
""",

    r"e:\FYP\SmartHire\communications\urls.py": """from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CommunicationViewSet

router = DefaultRouter()
router.register(r'comm', CommunicationViewSet, basename='communication')

urlpatterns = [
    path('', include(router.urls)),
]
""",

    r"e:\FYP\SmartHire\interviews\urls.py": """from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ScheduledInterviewViewSet

router = DefaultRouter()
router.register(r'schedule', ScheduledInterviewViewSet, basename='schedule')

urlpatterns = [
    path('', include(router.urls)),
]
""",

    r"e:\FYP\SmartHire\cv_bank\urls.py": """from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CVViewSet

router = DefaultRouter()
router.register(r'cvs', CVViewSet, basename='cv')

urlpatterns = [
    path('', include(router.urls)),
]
"""
}

for filepath, content in urls_to_fix.items():
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Updated {filepath}")
