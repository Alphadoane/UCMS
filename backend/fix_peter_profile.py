
import os
import django
import sys

sys.path.append(os.getcwd())
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.contrib.auth import get_user_model
from student_api.core.models import Lecturer, Faculty, Department

User = get_user_model()

try:
    u = User.objects.get(email="peter.pan@kcau.ac.ke")
    print(f"Found user: {u.email} (ID: {u.id})")
    
    if hasattr(u, 'lecturer'):
        print("Lecturer profile already exists.")
        print(f"Dept: {u.lecturer.department.name}")
        print(f"Emp ID: {u.lecturer.employee_id}")
    else:
        print("Creating Lecturer profile...")
        faculty, _ = Faculty.objects.get_or_create(name="FOCIM")
        department, _ = Department.objects.get_or_create(name="Software Engineering", defaults={'faculty': faculty})
        
        Lecturer.objects.create(
            user=u,
            department=department,
            employee_id="EMP-PETER-001",
            employment_type="Full Time"
        )
        print("Lecturer profile created.")

except User.DoesNotExist:
    print("User not found")
