from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import (
    RegisterView, ProfileView, CustomTokenObtainPairView,
    SearchCandidatesView, ProfileSetupView, GoogleLoginView,
    CandidateProfileDetailView, register_fcm_token,
)

urlpatterns = [
    path('register/', RegisterView.as_view(), name='auth_register'),
    path('login/', CustomTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('google-login/', GoogleLoginView.as_view(), name='google_login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('profile/', ProfileView.as_view(), name='profile'),
    path('profile/setup/', ProfileSetupView.as_view(), name='profile_setup'),
    path('search/candidates/', SearchCandidatesView.as_view(), name='search_candidates'),
    path('candidates/<int:pk>/', CandidateProfileDetailView.as_view(), name='candidate_detail'),
    path('fcm-token/', register_fcm_token, name='register_fcm_token'),
]
