from django.core.management.base import BaseCommand
from student_api.core.models import *
from django.utils import timezone
import datetime

class Command(BaseCommand):
    help = 'Seeds the database with production-grade data'

    def handle(self, *args, **kwargs):
        self.stdout.write('🌱 Seeding V2 Schema...')
        
        # 1. Roles
        r_admin = Role.objects.create(name='Admin')
        r_staff = Role.objects.create(name='Lecturer')
        r_student = Role.objects.create(name='Student')
        
        # 2. Org
        fac_comp = Faculty.objects.create(name='Faculty of Computing and Information Management')
        dept_cs = Department.objects.create(faculty=fac_comp, name='Computer Science')
        dept_it = Department.objects.create(faculty=fac_comp, name='Information Technology')
        
        # 3. Programs
        prog_bcs = Program.objects.create(department=dept_cs, code='BCS', name='Bachelor of Science in Computer Science', duration_years=4)
        prog_bit = Program.objects.create(department=dept_it, code='BIT', name='Bachelor of Science in Information Technology', duration_years=4)
        
        # 4. Academic Calendar
        ay_23_24 = AcademicYear.objects.create(year_label='2023/2024')
        sem_1 = Semester.objects.create(academic_year=ay_23_24, name='Semester 1', start_date='2023-09-01', end_date='2023-12-15')
        
        # 5. Courses
        c_algo = Course.objects.create(program=prog_bcs, code='CSC 301', name='Design and Analysis of Algorithms', credit_units=3)
        c_db = Course.objects.create(program=prog_bcs, code='CSC 302', name='Database Systems', credit_units=3)
        c_net = Course.objects.create(program=prog_bit, code='CIT 303', name='Network Programming', credit_units=3)
        
        # 6. Users - Admin
        if not User.objects.filter(email='admin@kca.ac.ke').exists():
            u_admin = User.objects.create_superuser(
                email='admin@kca.ac.ke', 
                password='password123',
                first_name='System',
                last_name='Admin'
            )
            UserRole.objects.create(user=u_admin, role=r_admin)
        
        # 7. Users - Student
        if not User.objects.filter(email='student1@kca.ac.ke').exists():
            u_student = User.objects.create_user(
                email='student1@kca.ac.ke', 
                password='password123',
                first_name='John',
                last_name='Doe'
            )
            UserRole.objects.create(user=u_student, role=r_student)
            
            # 8. Profiles
            s_profile = Student.objects.create(
                user=u_student,
                admission_number='22/04168',
                program=prog_bcs,
                admission_date='2022-09-01',
                status='active'
            )
            
            # 9. Registration
            CourseRegistration.objects.create(student=s_profile, course=c_algo, semester=sem_1)
            CourseRegistration.objects.create(student=s_profile, course=c_db, semester=sem_1)
            
            # 10. Finance
            FeeAccount.objects.create(student=s_profile, balance=15000.00)
            Invoice.objects.create(student=s_profile, semester=sem_1, amount=45000.00)

        self.stdout.write(self.style.SUCCESS('✅ Seeding Complete!'))
