from rest_framework import viewsets, status, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from django.utils import timezone
from student_api.academics.models import Student
from ..models import CampusLifeContent, Appointment, EmergencyAlert
from rest_framework import serializers

# Serializers
class CampusLifeSerializer(serializers.ModelSerializer):
    class Meta:
        model = CampusLifeContent
        fields = '__all__'
        read_only_fields = ['author', 'created_at']

class AppointmentSerializer(serializers.ModelSerializer):
    student_name = serializers.ReadOnlyField(source='student.full_name')
    class Meta:
        model = Appointment
        fields = '__all__'
        read_only_fields = ['status', 'admin_notes', 'created_at']

class EmergencyAlertSerializer(serializers.ModelSerializer):
    student_name = serializers.ReadOnlyField(source='student.full_name')
    class Meta:
        model = EmergencyAlert
        fields = '__all__'
        read_only_fields = ['status', 'created_at']

# ViewSets
class CampusLifeViewSet(viewsets.ModelViewSet):
    queryset = CampusLifeContent.objects.all()
    serializer_class = CampusLifeSerializer
    
    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [permissions.IsAdminUser()]
        return [permissions.IsAuthenticated()]

    def perform_create(self, serializer):
        serializer.save(author=self.request.user)

class AppointmentViewSet(viewsets.ModelViewSet):
    queryset = Appointment.objects.all()
    serializer_class = AppointmentSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        if user.is_staff:
            return Appointment.objects.all()
        try:
            student = Student.objects.get(user=user)
            return Appointment.objects.filter(student=student)
        except Student.DoesNotExist:
            return Appointment.objects.none()

    def perform_create(self, serializer):
        student = Student.objects.get(user=self.request.user)
        serializer.save(student=student)

    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def confirm(self, request, pk=None):
        appointment = self.get_object()
        appointment.status = 'CONFIRMED'
        appointment.admin_notes = request.data.get('admin_notes', '')
        appointment.save()
        return Response({'status': 'appointment confirmed'})

class EmergencyAlertViewSet(viewsets.ModelViewSet):
    queryset = EmergencyAlert.objects.all()
    serializer_class = EmergencyAlertSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        if self.request.user.is_staff:
            return EmergencyAlert.objects.all()
        try:
            student = Student.objects.get(user=self.request.user)
            return EmergencyAlert.objects.filter(student=student)
        except Student.DoesNotExist:
            return EmergencyAlert.objects.none()

    def perform_create(self, serializer):
        student = Student.objects.get(user=self.request.user)
        serializer.save(student=student)

    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def resolve(self, request, pk=None):
        alert = self.get_object()
        alert.status = 'RESOLVED'
        alert.save()
        return Response({'status': 'alert resolved'})
