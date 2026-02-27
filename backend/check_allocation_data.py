import os
import django
import sys

# Add the project root logic
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))))
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Course, Lecturer, Student
from student_api.core.serializers import StudentOptionSerializer, LecturerOptionSerializer, CourseOptionSerializer

print(f"Active Courses: {Course.objects.count()}")
print(f"Active Lecturers: {Lecturer.objects.count()}")
print(f"Active Students: {Student.objects.count()}")

print("\n--- SAMPLE DATA ---")
courses = Course.objects.all()[:1]
if courses:
    print(f"Course: {CourseOptionSerializer(courses[0]).data}")

lecturers = Lecturer.objects.all()[:1]
if lecturers:
    print(f"Lecturer: {LecturerOptionSerializer(lecturers[0]).data}")

students = Student.objects.all()[:1]
if students:
    print(f"Student: {StudentOptionSerializer(students[0]).data}")
