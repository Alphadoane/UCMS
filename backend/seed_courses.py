import os
import django
import random

# Setup Django environment
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.academics.models import Program, Course, Department, Faculty

def seed_courses():
    print("Beginning course seeding...")

    # Ensure Faculties exist
    scit, _ = Faculty.objects.get_or_create(name="School of Computing and Information Technology")
    edu, _ = Faculty.objects.get_or_create(name="School of Education")
    biz, _ = Faculty.objects.get_or_create(name="School of Business")

    # Ensure Departments exist
    dept_cs, _ = Department.objects.get_or_create(faculty=scit, name="Computer Science")
    dept_edu, _ = Department.objects.get_or_create(faculty=edu, name="Education Arts")
    dept_biz, _ = Department.objects.get_or_create(faculty=biz, name="Business Administration")

    # Define Programs and their Courses
    programs_data = [
        {
            "code": "Bsc. CS",
            "name": "Bachelor of Science in Computer Science",
            "department": dept_cs,
            "courses": [
                ("CS101", "Introduction to Programming", 3),
                ("CS102", "Computer Architecture", 3),
                ("CS103", "Discrete Mathematics", 3),
                ("CS201", "Data Structures & Algorithms", 4),
                ("CS202", "Operating Systems", 4),
                ("CS203", "Database Systems", 4),
                ("CS301", "Software Engineering", 4),
                ("CS302", "Artificial Intelligence", 4),
                ("CS401", "Distributed Systems", 4),
                ("CS402", "Computer Security", 4),
            ]
        },
        {
            "code": "B.Ed Arts",
            "name": "Bachelor of Education (Arts)",
            "department": dept_edu,
            "courses": [
                ("ED101", "History of Education", 3),
                ("ED102", "Philosophy of Education", 3),
                ("ED201", "Curriculum Development", 3),
                ("ED202", "Psychology of Learning", 3),
                ("ED301", "Educational Administration", 3),
                ("ED302", "Educational Technology", 3),
            ]
        },
        {
            "code": "B.Com",
            "name": "Bachelor of Commerce",
            "department": dept_biz,
            "courses": [
                ("BC101", "Introduction to Business", 3),
                ("BC102", "Financial Accounting", 3),
                ("BC201", "Principles of Marketing", 3),
                ("BC202", "Human Resource Management", 3),
                ("BC301", "Business Law", 3),
                ("BC302", "Strategic Management", 3),
            ]
        }
    ]

    for p_data in programs_data:
        # Create/Get Program
        program, created = Program.objects.get_or_create(
            code=p_data["code"],
            defaults={
                "name": p_data["name"],
                "department": p_data["department"],
                "duration_years": 4
            }
        )
        if created:
            print(f"Created Program: {program.name}")
        else:
            print(f"Found Program: {program.name}")

        # Create Courses
        for code, name, credits in p_data["courses"]:
            course, c_created = Course.objects.get_or_create(
                program=program,
                code=code,
                defaults={
                    "name": name,
                    "credit_units": credits
                }
            )
            if c_created:
                print(f"  - Added Course: {code} - {name}")
            else:
                print(f"  - Course exists: {code}")

    print("Course seeding completed successfully.")

if __name__ == "__main__":
    seed_courses()
