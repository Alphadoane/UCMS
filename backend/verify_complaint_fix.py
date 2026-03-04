import os
import django
import json
from rest_framework.test import APIClient
from django.contrib.auth import get_user_model

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.conf import settings
from rest_framework.test import APIClient
from django.contrib.auth import get_user_model

User = get_user_model()

def verify_complaints():
    client = APIClient()
    print("Django version:", django.get_version())
    
    # 1. Create a student user if not exists
    student_user, _ = User.objects.get_or_create(
        email='test_student@example.com',
        defaults={'first_name': 'Test', 'last_name': 'Student', 'username': 'test_student'}
    )
    student_user.set_password('password')
    student_user.save()

    from student_api.academics.models import Student, Program, Course
    program = Program.objects.first()
    if not program:
        print("No program found, cannot test")
        return
        
    student_profile, _ = Student.objects.get_or_create(
        user=student_user,
        defaults={'admission_number': 'T001', 'program': program, 'status': 'active', 'admission_date': '2023-01-01'}
    )
    
    course = Course.objects.first()
    if not course:
        print("No course found, cannot test")
        return

    # 2. Login
    client.force_authenticate(user=student_user)
    
    # 3. Test POST (Submission)
    print("Testing Complaint Submission...")
    data = {
        "course_id": course.id,
        "description": "Verification test complaint description",
        "priority": "high"
    }
    response = client.post('/api/support/tickets', data, format='json')
    if response.status_code == 201:
        print("✓ Complaint Submission Successful")
        complaint_id = response.data['id']
    else:
        print(f"✗ Complaint Submission Failed: {response.status_code} - {response.data}")
        return

    # 4. Test GET (List) - Verify title and category
    print("\nTesting Complaint List (Student)...")
    response = client.get('/api/support/tickets')
    if response.status_code == 200:
        items = response.data.get('items', [])
        if items:
            item = items[0]
            print(f"✓ Complaint List retrieved. Count: {len(items)}")
            print(f"✓ Field 'title' present: {item.get('title') == item.get('description')}")
            print(f"✓ Field 'category' present: {item.get('category') == 'Academic'}")
            print(f"✓ Field 'course_name' present: {item.get('course_name') is not null}")
        else:
            print("✗ No complaints found in list")
    else:
        print(f"✗ Complaint List Failed: {response.status_code}")

    # 5. Test Admin Access
    print("\nTesting Admin Access...")
    admin_user = User.objects.filter(is_superuser=True).first()
    if admin_user:
        client.force_authenticate(user=admin_user)
        response = client.get('/api/support/tickets')
        if response.status_code == 200:
             print(f"✓ Admin retrieved list. Total complaints: {len(response.data.get('items', []))}")
        else:
             print(f"✗ Admin access failed: {response.status_code}")
    else:
        print("! No admin user found to test")

if __name__ == "__main__":
    verify_complaints()
