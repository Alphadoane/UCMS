import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Course, CourseRegistration, Student

def diag():
    courses = Course.objects.all()
    print(f"Total Courses: {courses.count()}")
    for c in courses:
        regs = CourseRegistration.objects.filter(course=c)
        print(f"Course {c.code} ({c.id}): {regs.count()} students")
        for r in regs:
            print(f"  - {r.student.admission_number}: {r.student.user.first_name} {r.student.user.last_name}")

if __name__ == "__main__":
    diag()
