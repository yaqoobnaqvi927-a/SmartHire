from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import JobPostingViewSet, ApplicationViewSet

router = DefaultRouter()
router.register(r'jobs', JobPostingViewSet, basename='jobposting')
router.register(r'applications', ApplicationViewSet, basename='application')

urlpatterns = [
    path('search/', JobPostingViewSet.as_view({'get': 'search'}), name='job-search'),
    path('', include(router.urls)),
]
