from rest_framework import serializers
from .models import AdmissionApplication, AdmissionDocument
from student_api.academics.models import Program

class ProgramSerializer(serializers.ModelSerializer):
    class Meta:
        model = Program
        fields = ['id', 'code', 'name', 'duration_years', 'category', 'entry_requirements']

class AdmissionDocumentSerializer(serializers.ModelSerializer):
    class Meta:
        model = AdmissionDocument
        fields = ['id', 'document_type', 'file', 'uploaded_at', 'is_verified', 'rejection_reason']
        read_only_fields = ['id', 'uploaded_at', 'is_verified', 'rejection_reason']

class AdmissionApplicationSerializer(serializers.ModelSerializer):
    documents = AdmissionDocumentSerializer(many=True, read_only=True)
    program_name = serializers.CharField(source='program_choice.name', read_only=True)
    missing_documents = serializers.ListField(read_only=True)
    
    class Meta:
        model = AdmissionApplication
        fields = '__all__'
        read_only_fields = ['id', 'application_id', 'application_date', 'current_phase', 'rejection_reason']
        
    def validate_national_id(self, value):
        if AdmissionApplication.objects.filter(national_id=value).exists():
            raise serializers.ValidationError("An application with this National ID already exists.")
        # Also check Student table if robust
        return value

class ApplicationStatusSerializer(serializers.ModelSerializer):
    documents = AdmissionDocumentSerializer(many=True, read_only=True)
    missing_documents = serializers.ListField(read_only=True)
    credential_password = serializers.CharField(source='national_id', read_only=True)
    student_reg_number = serializers.SerializerMethodField()
    
    class Meta:
        model = AdmissionApplication
        fields = ['application_id', 'first_name', 'current_phase', 'application_date', 'documents', 'missing_documents', 'email', 'credential_password', 'student_reg_number']

    def get_student_reg_number(self, obj):
        if obj.current_phase == 'ENROLLED' and obj.email:
             from student_api.academics.models import Student
             student = Student.objects.filter(user__email=obj.email).first()
             return student.admission_number if student else None
        return None
