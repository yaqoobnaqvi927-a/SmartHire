from rest_framework import serializers
from .models import User, CandidateProfile, RecruiterProfile
from django.contrib.auth import authenticate

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'role_type', 'full_name', 'first_name', 'last_name')

class CandidateProfileSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    
    class Meta:
        model = CandidateProfile
        fields = '__all__'
        read_only_fields = ('search_keywords_index', 'vector_profile', 'profile_completeness')

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
            RecruiterProfile.objects.create(user=user, company_name='')
        return user
