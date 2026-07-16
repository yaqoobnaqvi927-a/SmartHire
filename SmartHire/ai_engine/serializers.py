from rest_framework import serializers
from .models import AIMatchReport


class AIMatchReportSerializer(serializers.ModelSerializer):
    """Read-only serializer for the cached AI match report on an application."""

    class Meta:
        model = AIMatchReport
        fields = '__all__'
        read_only_fields = ['generated_at']
