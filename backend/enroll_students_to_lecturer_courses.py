import os
import django
from datetime import date

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Course, CourseRegistration, Student, Semester, AcademicYear
from student_api.core.models import User

def enroll():
    # Find a student
    try:
        student = Student.objects.first()
        if not student:
            print("No students found in DB")
            return
    except Exception as e:
        print(f"Error finding student: {e}")
        return

    # Find or create academic year
    ay, _ = AcademicYear.objects.get_or_create(year_label="2025/2026")

    # Find or create semester
    semester, _ = Semester.objects.get_or_create(
        academic_year=ay,
        name="Semester 1",
        defaults={
            'start_date': date(2026, 1, 5),
            'end_date': date(2026, 4, 30)
        }
    )

    # Courses assigned to lecturer@kcau.ac.ke
    course_codes = ['SE401', 'MATH101', 'CS102']
    courses = Course.objects.filter(code__in=course_codes)

    if not courses.exists():
        print("No matching courses found for lecturer")
        return

    print(f"Enrolling student {student.admission_number} ({student.user.email}) to lecturer's courses...")
    for course in courses:
        reg, created = CourseRegistration.objects.get_or_create(
            student=student,
            course=course,
            semester=semester
        )
        if created:
            print(f"  - Enrolled in {course.code}")
        else:
            print(f"  - Already enrolled in {course.code}")

if __name__ == "__main__":
    enroll()
