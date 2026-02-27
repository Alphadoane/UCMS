import logging
import random
from django.utils import timezone
from django.contrib.auth import get_user_model
from student_api.core.models import (
    UserRole, Role, SystemSettings
)
from student_api.academics.models import (
    Faculty, Department, Program, Student, Lecturer
)
# We might need AdmissionApplication but it causes circular import if models are cross-referenced incorrectly.
# Ideally services shouldn't depend on too many things, but here we process logic.

logger = logging.getLogger(__name__)
User = get_user_model()

class StudentIdentityService:
    @staticmethod
    def generate_next_identity():
        """
        Generates the next registration number and student email based on SystemSettings.
        Format: RegNo: 30/00001, Email: 3000001@students.kcau.ac.ke
        """
        from django.db import transaction
        with transaction.atomic():
            settings, _ = SystemSettings.objects.select_for_update().get_or_create(id=1)
            settings.last_student_seq += 1
            settings.save()
            
            prefix = settings.student_id_prefix
            seq = settings.last_student_seq
            domain = settings.student_email_domain
            
            reg_number = f"{prefix}/{seq:05d}"
            email = f"{prefix}{seq:05d}@{domain}"
            
            return {
                "reg_number": reg_number,
                "email": email
            }

class UserService:
    @staticmethod
    def create_user_logic(data):
        """
        Handles the logic for creating a user, assigning roles, and creating profiles.
        Returns a dictionary with result data or raises Exception.
        """
        full_name = data.get('full_name')
        initial_email = data.get('email')
        password = data.get('password', 'password123')
        role_name = data.get('role', 'student').lower()
        reg_number = data.get('reg_number')

        if not full_name:
            raise ValueError("Full Name is required")

        # 1. Generate Email/RegNo if student
        if role_name == 'student' and not initial_email:
            identity = StudentIdentityService.generate_next_identity()
            email = identity['email']
            reg_number = identity['reg_number']
        elif not initial_email:
            names = full_name.lower().split()
            if len(names) >= 2:
                base_email = f"{names[0]}.{names[1]}"
            else:
                base_email = names[0]
            
            domain = SystemSettings.get_domain()
            email = f"{base_email}@{domain}"
            
            # Simple collision handling
            counter = 1
            while User.objects.filter(email=email).exists():
                email = f"{base_email}{counter}@{domain}"
                counter += 1
        else:
            email = initial_email
            if User.objects.filter(email=email).exists():
                raise ValueError("User with this email already exists")

        # 2. Create User
        user = User.objects.create_user(email=email, password=password)
        names = full_name.split(' ', 1)
        user.first_name = names[0]
        user.last_name = names[1] if len(names) > 1 else ''
        
        # 3. Assign Role and Permissions
        if role_name == 'admin':
            user.is_staff = True
            user.is_superuser = True
        elif role_name == 'staff':
            user.is_staff = True
            user.save()
            
            # Create Lecturer Profile
            dept_name = data.get('department', 'General')
            emp_id = reg_number if reg_number else f"EMP-{user.id.hex[:6]}"
            
            faculty, _ = Faculty.objects.get_or_create(name="FOCIM")
            department, _ = Department.objects.get_or_create(name=dept_name, defaults={'faculty': faculty})
            
            Lecturer.objects.create(
                user=user,
                department=department,
                employee_id=emp_id,
                employment_type="Full Time"
            )
        
        user.save()
        
        # Create Role entry
        role_obj, _ = Role.objects.get_or_create(name=role_name)
        UserRole.objects.create(user=user, role=role_obj)
        
        # 4. Create Student Profile if needed
        if role_name == 'student':
            if not reg_number:
                reg_number = f"KCA/{random.randint(1000,9999)}/2026"
                
            program = Program.objects.first()
            if not program:
                 faculty, _ = Faculty.objects.get_or_create(name="FOCIM")
                 dept, _ = Department.objects.get_or_create(name="Software Engineering", faculty=faculty)
                 program = Program.objects.create(
                     department=dept, 
                     code="BSc.SE", 
                     name="Bachelor of Science in Software Engineering",
                     duration_years=4
                 )
            
            Student.objects.create(
                user=user,
                admission_number=reg_number,
                program=program,
                admission_date=timezone.now().date(),
                status='active'
            )
            
        return {
            "id": user.id,
            "email": user.email,
            "full_name": full_name,
            "role": role_name,
            "reg_number": reg_number if role_name == 'student' else None
        }

class EnrollmentService:
    @staticmethod
    def enroll_application(app):
        """
        Takes an AdmissionApplication object (from 'admission' app) and enrolls it.
        """
        # 1. Generate Student Identity
        identity = StudentIdentityService.generate_next_identity()
        reg_number = identity['reg_number']
        generated_email = identity['email']
        
        # 3. Create or Get User Identity
        user = User.objects.filter(email=generated_email).first()
        temp_password = app.national_id 

        if not user:
            user = User.objects.create_user(email=generated_email, password=temp_password)
            user.first_name = app.first_name
            user.last_name = app.last_name
            user.save()
            
            # Assign 'student' role
            role_obj, _ = Role.objects.get_or_create(name='student')
            UserRole.objects.create(user=user, role=role_obj)
        else:
            if hasattr(user, 'student_profile'):
                 raise ValueError("User already exists and is enrolled")

        # 4. Create Institutional Profile (Student)
        Student.objects.create(
            user=user,
            admission_number=reg_number,
            program=app.program_choice,
            admission_date=timezone.now().date(),
            status='active'
        )
        
        return {
            "reg_number": reg_number,
            "email": generated_email,
            "temp_password": temp_password
        }
