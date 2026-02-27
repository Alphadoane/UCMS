from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import IsAdminUser
from student_api.core.models import User
from student_api.academics.models import Faculty, Department, Program
from student_api.support.models import Complaint
from django.db import connection

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_stats(request):
    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT 1")
        db_status = "Healthy"
    except Exception:
        db_status = "Unhealthy"

    return Response({
        "total_users": User.objects.count(),
        "pending_complaints": Complaint.objects.filter(status='submitted').count(),
        "db_status": db_status
    })

@api_view(["POST"])
@permission_classes([IsAdminUser])
def admin_create_user(request):
    return Response({"status": "Created"})

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_users_list(request):
    users = User.objects.all()
    data = []
    for user in users:
        role = "STUDENT"
        if user.is_superuser: role = "ADMIN"
        elif user.is_staff: role = "STAFF"
        
        data.append({
            "id": str(user.id),
            "email": user.email,
            "role": role,
            "first_name": user.first_name,
            "last_name": user.last_name,
            "admission_no": getattr(user, 'admission_number', None) if not user.is_staff else None,
            "employee_id": None # Placeholder
        })
    return Response(data)

@api_view(["POST"])
@permission_classes([IsAdminUser])
def send_broadcast(request):
    return Response({"status": "Sent"})
