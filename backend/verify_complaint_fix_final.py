import os
import django
import sys

# Standardize output for Windows console
if sys.stdout.encoding != 'utf-8':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# SET SETTINGS FIRST
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

# THEN IMPORT DJANGO/DRF COMPONENTS
from rest_framework.test import APIClient
from django.contrib.auth import get_user_model
from student_api.academics.models import Student, Program, Course, Lecture
from student_api.support.models import Complaint

User = get_user_model()

def verify_complaints():
    client = APIClient()
    print("Django version:", django.get_version())
    
    # 1. Create a student user if not exists
    student_user, _ = User.objects.get_or_create(
        email='test_student_fix_v2@example.com',
        defaults={'first_name': 'Test', 'last_name': 'Student'}
    )
    student_user.set_password('password')
    student_user.save()

    program = Program.objects.first()
    if not program:
        print("No program found.")
        return
        
    student_profile, _ = Student.objects.get_or_create(
        user=student_user,
        defaults={'admission_number': 'T001_FIX_V2', 'program': program, 'status': 'active', 'admission_date': '2023-01-01'}
    )
    
    course = Course.objects.first()
    if not course:
        print("No course found.")
        return

    # 2. Login
    client.force_authenticate(user=student_user)
    
    # 3. Test POST (Submission)
    print("\nTesting Complaint Submission...")
    data = {
        "course_id": course.id,
        "description": "Validation test V3 Multipart",
        "priority": "high"
    }
    # Backend uses MultiPartParser
    response = client.post('/api/support/tickets', data, format='multipart')
    if response.status_code == 201:
        print("OK: Complaint Submission Successful")
        complaint_id = response.data['id']
    else:
        print(f"FAIL: Complaint Submission Failed: {response.status_code} - {response.data}")
        return

    # 4. Test GET (List) - Verify title and category are present for frontend
    print("\nTesting Complaint List Content...")
    response = client.get('/api/support/tickets')
    if response.status_code == 200:
        items = response.data.get('items', [])
        if items:
            item = next((i for i in items if i['id'] == complaint_id), items[0])
            print(f"OK: Complaint List retrieved. Count: {len(items)}")
            
            # Check for the alias fields we added
            has_title = 'title' in item
            has_category = 'category' in item
            
            print(f"Field 'title' present: {has_title} (Value: {item.get('title')})")
            print(f"Field 'category' present: {has_category} (Value: {item.get('category')})")
            
            if has_title and has_category:
                print("\nSUCCESS: Backend fix verified.")
            else:
                print("\nFAILURE: Backend fix missing required fields.")
        else:
            print("FAIL: No complaints found in list")
    else:
        print(f"FAIL: Complaint List Failed: {response.status_code}")

if __name__ == "__main__":
    verify_complaints()
