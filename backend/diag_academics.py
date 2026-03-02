import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Lecturer, Course, Lecture, Department
from student_api.core.models import User, UserRole

def diag():
    print("--- Users & Roles ---")
    for u in User.objects.all():
        roles = ", ".join([ur.role.name for ur in UserRole.objects.filter(user=u)])
        print(f"ID: {u.id}, Email: {u.email}, Roles: [{roles}]")
        
    print("\n--- Lecturers ---")
    for l in Lecturer.objects.all():
        print(f"User: {l.user.email}, EmployeeID: {l.employee_id}, Dept: {l.department.name}")
        
    print("\n--- Lectures (Assignments) ---")
    for lec in Lecture.objects.all():
        print(f"Course: {lec.course.code}, Lecturer: {lec.lecturer.user.email}, Venue: {lec.venue}")

if __name__ == "__main__":
    diag()
