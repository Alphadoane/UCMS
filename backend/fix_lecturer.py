import os
import django
import sys

sys.path.append('d:/Android/backend')
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.core.models import User
from student_api.academics.models import Lecturer, Department, Faculty

def fix_lecturer_account():
    email = "lecturer@kcau.ac.ke"
    try:
        user = User.objects.get(email=email)
        print(f"Found user: {email}")
    except User.DoesNotExist:
        print(f"User {email} not found. Creating...")
        user = User.objects.create_user(email=email, password="password123", first_name="John", last_name="Lecturer")
    
    # Ensure is_staff is True (optional, but good for admin access if needed, though LECTURER role is distinct)
    user.is_staff = True
    user.save()
    print(f"User {email} is_staff set to True")

    # Check/Create Lecturer Profile
    try:
        lecturer = Lecturer.objects.get(user=user)
        print(f"Lecturer profile exists: {lecturer.employee_id}")
    except Lecturer.DoesNotExist:
        print("Creating Lecturer profile...")
        # Get or Create Faculty/Department
        faculty, _ = Faculty.objects.get_or_create(name="Faculty of Science")
        dept, _ = Department.objects.get_or_create(name="Computer Science", faculty=faculty)
        
        lecturer = Lecturer.objects.create(
            user=user,
            department=dept,
            employee_id="EMP-DEFAULT-001",
            employment_type="Full-Time"
        )
        print(f"Lecturer profile created: {lecturer.employee_id}")

if __name__ == "__main__":
    fix_lecturer_account()
