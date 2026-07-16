from rest_framework import serializers
from .models import CV

class CVSerializer(serializers.ModelSerializer):
    class Meta:
        model = CV
        fields = '__all__'
        read_only_fields = ('user', 'extracted_text', 'skills_extracted', 'uploaded_at')

    def create(self, validated_data):
        user = self.context['request'].user
        validated_data['user'] = user
        cv = super().create(validated_data)
        
        # Trigger parsing service synchronously for MVP
        try:
            from .services import process_uploaded_cv
            process_uploaded_cv(cv)
        except Exception as e:
            print("CV Parsing failed:", e)

        return cv
