import os
import sys
import django
from datetime import date, timedelta
from django.utils import timezone

# Add the project root to sys.path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.core.models import User, Role, UserRole, SystemSettings
from student_api.academics.models import (
    Faculty, Department, Program, Course, AcademicYear, Semester, Student, Lecturer, CourseRegistration
)
from student_api.finance.models import FeeAccount

def seed_db():
    print("Starting Database Seeding...")

    # 1. System Settings
    SystemSettings.objects.get_or_create(id=1, defaults={'student_email_domain': 'kcau.ac.ke'})
    print("System Settings initialized.")

    # 2. Roles
    student_role, _ = Role.objects.get_or_create(name='STUDENT')
    staff_role, _ = Role.objects.get_or_create(name='STAFF')
    admin_role, _ = Role.objects.get_or_create(name='ADMIN')
    print("Roles created.")

    # 3. Users
    # Admin
    admin_user, created = User.objects.get_or_create(
        email='admin@kcau.ac.ke',
        defaults={
            'first_name': 'System',
            'last_name': 'Administrator',
            'is_staff': True,
            'is_superuser': True
        }
    )
    if created:
        admin_user.set_password('admin123')
        admin_user.save()
        UserRole.objects.get_or_create(user=admin_user, role=admin_role)

    # Student 1
    s1_user, created = User.objects.get_or_create(
        email='student1@kcau.ac.ke',
        defaults={'first_name': 'John', 'last_name': 'Doe'}
    )
    if created:
        s1_user.set_password('student123')
        s1_user.save()
        UserRole.objects.get_or_create(user=s1_user, role=student_role)

    # Lecturer 1
    l1_user, created = User.objects.get_or_create(
        email='lecturer1@kcau.ac.ke',
        defaults={'first_name': 'Jane', 'last_name': 'Smith', 'is_staff': True}
    )
    if created:
        l1_user.set_password('lecturer123')
        l1_user.save()
        UserRole.objects.get_or_create(user=l1_user, role=staff_role)
    print("Users created.")

    # 4. Organization
    faculty, _ = Faculty.objects.get_or_create(name='School of Technology')
    dept, _ = Department.objects.get_or_create(faculty=faculty, name='Computing & Information Technology')
    print("Organization structure created.")

    # 5. Programs & Courses
    program, _ = Program.objects.get_or_create(
        department=dept,
        code='BSCIT',
        defaults={
            'name': 'Bachelor of Science in Information Technology',
            'duration_years': 4,
            'category': 'Undergraduate'
        }
    )

    c1, _ = Course.objects.get_or_create(program=program, code='CIT1101', defaults={'name': 'Introduction to Programming', 'credit_units': 3})
    c2, _ = Course.objects.get_or_create(program=program, code='CIT1102', defaults={'name': 'Database Systems', 'credit_units': 3})
    print("Programs and Courses created.")

    # 6. Academic Calendar
    ay, _ = AcademicYear.objects.get_or_create(year_label='2025/2026')
    sem, _ = Semester.objects.get_or_create(
        academic_year=ay,
        name='Semester 1',
        defaults={
            'start_date': date(2025, 1, 1),
            'end_date': date(2025, 4, 30)
        }
    )
    print("Academic Calendar created.")

    # 7. Profiles
    student, _ = Student.objects.get_or_create(
        user=s1_user,
        defaults={
            'admission_number': 'CIT/001/2025',
            'program': program,
            'admission_date': date(2025, 1, 1),
            'status': 'active'
        }
    )

    lecturer, _ = Lecturer.objects.get_or_create(
        user=l1_user,
        defaults={
            'department': dept,
            'employee_id': 'EMP001',
            'employment_type': 'Full-time'
        }
    )
    print("Student and Lecturer profiles created.")

    # 8. Course Registration
    CourseRegistration.objects.get_or_create(student=student, course=c1, semester=sem)
    CourseRegistration.objects.get_or_create(student=student, course=c2, semester=sem)
    print("Course Registrations created.")

    # 9. Finance
    FeeAccount.objects.get_or_create(student=student, defaults={'balance': 50000.00})
    print("Finance accounts created.")

    print("\nSeeding Completed Successfully!")

if __name__ == "__main__":
    seed_db()
