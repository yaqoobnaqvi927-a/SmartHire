from rest_framework import serializers
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
