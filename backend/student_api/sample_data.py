from django.contrib.auth.models import User
from student_api.core.models import (
    StudentProfile, CourseRegistration, CourseWork, ExamResult, 
    FeePayment, Receipt, FinanceRecord, Election, Vote, ZoomRoom
)
from student_api.auth_service import AuthService
import logging
from datetime import datetime, date

logger = logging.getLogger(__name__)

def create_sample_users():
    """Create sample users for testing"""
    sample_users = [
        {
            "username": "student1",
            "password": "password123",
            "email": "student1@kca.ac.ke",
            "first_name": "John",
            "last_name": "Doe",
            "admission_no": "22/04168",
        },
        {
            "username": "student2", 
            "password": "password123",
            "email": "student2@kca.ac.ke",
            "first_name": "Jane",
            "last_name": "Smith",
            "admission_no": "22/04169",
        },
        {
            "username": "admin",
            "password": "admin123",
            "email": "admin@kca.ac.ke",
            "first_name": "Admin",
            "last_name": "User",
            "admission_no": "ADMIN001",
        }
    ]
    
    for user_data in sample_users:
        # Check if user already exists
        user, created = User.objects.get_or_create(
            username=user_data["username"],
            defaults={
                'email': user_data["email"],
                'first_name': user_data["first_name"],
                'last_name': user_data["last_name"],
            }
        )
        
        if created:
            user.set_password(user_data["password"])
            user.save()
            logger.info(f"Created sample user: {user_data['username']}")
        else:
            logger.info(f"User {user_data['username']} already exists")
        
        # Create student profile
        profile, profile_created = StudentProfile.objects.get_or_create(
            user=user,
            defaults={
                'admission_no': user_data["admission_no"],
                'full_name': f"{user_data['first_name']} {user_data['last_name']}",
                'email': user_data["email"],
                'course': "Bachelor of Computer Science" if user_data["username"].startswith("student") else "Administration",
                'year_of_study': 3 if user_data["username"] == "student1" else 2,
                'semester': "Semester 1",
                'campus': "Main Campus"
            }
        )
        
        if profile_created:
            logger.info(f"Created student profile for: {user_data['username']}")
        else:
            logger.info(f"Student profile for {user_data['username']} already exists")

def create_sample_academic_data():
    """Create sample academic data"""
    try:
        # Get student profiles
        student1_profile = StudentProfile.objects.filter(admission_no="22/04168").first()
        student2_profile = StudentProfile.objects.filter(admission_no="22/04169").first()
        
        if student1_profile:
            # Course registrations for student1
            courses = [
                {"course_code": "CS301", "course_name": "Data Structures and Algorithms", "credits": 3, "semester": "Semester 1"},
                {"course_code": "CS302", "course_name": "Database Systems", "credits": 3, "semester": "Semester 1"},
                {"course_code": "CS303", "course_name": "Software Engineering", "credits": 3, "semester": "Semester 1"},
            ]
            
            for course_data in courses:
                CourseRegistration.objects.get_or_create(
                    student=student1_profile,
                    course_code=course_data["course_code"],
                    semester=course_data["semester"],
                    defaults=course_data
                )
            
            # Course work for student1
            CourseWork.objects.get_or_create(
                student=student1_profile,
                course_code="CS301",
                assignment="Assignment 1",
                defaults={
                    "marks": 85,
                    "max_marks": 100,
                    "due_date": date(2024, 1, 15),
                    "status": "Submitted"
                }
            )
            
            # Exam results for student1
            ExamResult.objects.get_or_create(
                student=student1_profile,
                course_code="CS301",
                exam_type="Midterm",
                defaults={
                    "marks": 78,
                    "max_marks": 100,
                    "grade": "B+",
                    "semester": "Semester 1"
                }
            )
            
            logger.info("Created sample academic data for student1")
        
        if student2_profile:
            # Course registrations for student2
            courses = [
                {"course_code": "IT201", "course_name": "Information Systems", "credits": 3, "semester": "Semester 2"},
                {"course_code": "IT202", "course_name": "Network Administration", "credits": 3, "semester": "Semester 2"},
            ]
            
            for course_data in courses:
                CourseRegistration.objects.get_or_create(
                    student=student2_profile,
                    course_code=course_data["course_code"],
                    semester=course_data["semester"],
                    defaults=course_data
                )
            
            logger.info("Created sample academic data for student2")
            
    except Exception as e:
        logger.error(f"Error creating academic data: {e}")

def create_sample_finance_data():
    """Create sample finance data"""
    try:
        student1_profile = StudentProfile.objects.filter(admission_no="22/04168").first()
        
        if student1_profile:
            # Create finance record
            finance_record, created = FinanceRecord.objects.get_or_create(
                student=student1_profile,
                defaults={'balance': 15000.00}
            )
            
            # Create fee payment
            FeePayment.objects.get_or_create(
                payment_id="PAY001",
                defaults={
                    'student': student1_profile,
                    'amount': 50000.00,
                    'payment_method': 'M-Pesa',
                    'date': date(2024, 1, 10),
                    'status': 'Completed',
                    'description': 'Semester 1 Fees'
                }
            )
            
            # Create receipt
            Receipt.objects.get_or_create(
                receipt_no="RCP001",
                defaults={
                    'student': student1_profile,
                    'amount': 50000.00,
                    'date': date(2024, 1, 10),
                    'description': 'Semester 1 Fees Payment'
                }
            )
            
            logger.info("Created sample finance data for student1")
            
    except Exception as e:
        logger.error(f"Error creating finance data: {e}")

def create_sample_voting_data():
    """Create sample voting data"""
    try:
        # Create sample elections
        election1, created = Election.objects.get_or_create(
            title="Student Council 2024",
            defaults={
                'candidates': ["Alice Johnson", "Bob Smith", "Carol Davis"],
                'results': {"Alice Johnson": 0, "Bob Smith": 0, "Carol Davis": 0}
            }
        )
        
        election2, created = Election.objects.get_or_create(
            title="Class Representative 2024",
            defaults={
                'candidates': ["David Wilson", "Eva Brown"],
                'results': {"David Wilson": 0, "Eva Brown": 0}
            }
        )
        
        logger.info("Created sample voting data")
        
    except Exception as e:
        logger.error(f"Error creating voting data: {e}")

def create_sample_virtual_campus_data():
    """Create sample virtual campus data"""
    try:
        # Create sample zoom rooms
        ZoomRoom.objects.get_or_create(
            room_id="csc101-1",
            defaults={
                'course_code': 'CSC101',
                'course_title': 'Introduction to Computing',
                'start_time': datetime(2024, 9, 15, 9, 0, 0),
                'join_url': 'https://zoom.us/j/1234567890?pwd=example'
            }
        )
        
        ZoomRoom.objects.get_or_create(
            room_id="mat201-1",
            defaults={
                'course_code': 'MAT201',
                'course_title': 'Discrete Mathematics',
                'start_time': datetime(2024, 9, 15, 11, 0, 0),
                'join_url': 'https://zoom.us/j/1987654321?pwd=example'
            }
        )
        
        logger.info("Created sample virtual campus data")
        
    except Exception as e:
        logger.error(f"Error creating virtual campus data: {e}")

def create_all_sample_data():
    """Create all sample data"""
    try:
        create_sample_users()
        create_sample_academic_data()
        create_sample_finance_data()
        create_sample_voting_data()
        create_sample_virtual_campus_data()
        logger.info("All sample data created successfully")
    except Exception as e:
        logger.error(f"Error creating sample data: {e}")

if __name__ == "__main__":
    create_all_sample_data()
