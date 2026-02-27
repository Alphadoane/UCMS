import os
import django
import sys
import datetime

sys.path.append(os.getcwd())
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Lecturer, Course, Lecture, Program, Department

try:
    lecturer = Lecturer.objects.get(user__email="peter.pan@kcau.ac.ke")
    print(f"Allocating lectures for {lecturer.user.first_name}")
    
    # Ensure Courses exist
    dept = lecturer.department
    prog, _ = Program.objects.get_or_create(code="BSc.SE", defaults={'name': 'Software Engineering', 'department': dept, 'duration_years': 4})
    
    c1, _ = Course.objects.get_or_create(code="SE312", program=prog, defaults={'name': 'Clean Code Architecture', 'credit_units': 3})
    c2, _ = Course.objects.get_or_create(code="SE401", program=prog, defaults={'name': 'Advanced Algorithms', 'credit_units': 4})
    
    # Clear existing
    Lecture.objects.filter(lecturer=lecturer).delete()
    
    # Create Lectures
    Lecture.objects.create(
        course=c1, lecturer=lecturer, day_of_week="Monday", 
        start_time=datetime.time(9, 0), end_time=datetime.time(11, 0), venue="Lab 4"
    )
    Lecture.objects.create(
        course=c2, lecturer=lecturer, day_of_week="Wednesday", 
        start_time=datetime.time(14, 0), end_time=datetime.time(16, 0), venue="Hall C"
    )
    Lecture.objects.create(
        course=c1, lecturer=lecturer, day_of_week="Friday", 
        start_time=datetime.time(10, 0), end_time=datetime.time(12, 0), venue="Online"
    )
    
    print("Lectures allocated.")

except Lecturer.DoesNotExist:
    print("Lecturer peter.pan not found")
except Exception as e:
    print(f"Error: {e}")
