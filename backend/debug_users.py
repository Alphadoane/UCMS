import os
import django
import sys

# Add the project root to sys.path
sys.path.append('d:/Android/backend')

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.core.models import User
from student_api.academics.models import Lecturer, Student

print("--- Users ---")
for u in User.objects.all():
    print(f"User: {u.email} (ID: {u.id}) - Staff: {u.is_staff}, Superuser: {u.is_superuser}")
    
    # Check Lecturer
    try:
        lecturer = Lecturer.objects.get(user=u)
        print(f"  -> IS LECTURER: {lecturer.employee_id}")
    except Lecturer.DoesNotExist:
        pass

    # Check Student
    try:
        student = Student.objects.get(user=u)
        print(f"  -> IS STUDENT: {student.admission_number}")
    except Student.DoesNotExist:
        pass

print("--- End ---")
