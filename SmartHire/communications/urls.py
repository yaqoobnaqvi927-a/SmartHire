from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CommunicationViewSet

router = DefaultRouter()
router.register(r'comm', CommunicationViewSet, basename='communication')

urlpatterns = [
    path('', include(router.urls)),
]
