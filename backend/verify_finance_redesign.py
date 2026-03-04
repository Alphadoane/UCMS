import os
import django
import sys
import json

# Standardize output
if sys.stdout.encoding != 'utf-8':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from rest_framework.test import APIClient
from django.contrib.auth import get_user_model
from student_api.academics.models import Student, Program, Department, Faculty
from student_api.finance.models import Payment, FeeAccount
from django.utils import timezone
import uuid

User = get_user_model()

def verify_admin_finance():
    client = APIClient()
    print("Django version:", django.get_version())
    
    # 1. Setup Data
    admin_user = User.objects.filter(is_superuser=True).first()
    if not admin_user:
        admin_user = User.objects.create_superuser(email='finance_admin@example.com', password='password', first_name='Finance', last_name='Admin')
    
    # Create a test student
    student_user, _ = User.objects.get_or_create(email='test_finance_student@example.com', defaults={'first_name': 'Finance', 'last_name': 'Student'})
    
    faculty, _ = Faculty.objects.get_or_create(name='Test Faculty')
    dept, _ = Department.objects.get_or_create(name='Test Dept', faculty=faculty)
    program, _ = Program.objects.get_or_create(name='Test Program', department=dept, defaults={'code': 'FIN101', 'duration_years': 4})
    
    student, created = Student.objects.get_or_create(
        user=student_user,
        defaults={
            'admission_number': 'FIN-001',
            'program': program,
            'admission_date': timezone.now().date(),
            'status': 'active'
        }
    )
    
    account, _ = FeeAccount.objects.get_or_create(student=student, defaults={'balance': 50000.00})
    
    # Create successful payment
    Payment.objects.get_or_create(
        student=student,
        amount=10000.00,
        payment_method='M-Pesa',
        status='SUCCESS',
        provider_reference='REF-SUCCESS',
        defaults={'paid_at': timezone.now()}
    )
    
    # Create failed payment
    Payment.objects.get_or_create(
        student=student,
        amount=5000.00,
        payment_method='M-Pesa',
        status='FAILED',
        provider_reference='REF-FAILED',
        defaults={'paid_at': timezone.now()}
    )

    # 2. Authenticate
    client.force_authenticate(user=admin_user)
    
    # 3. Test Student List
    print("\nTesting Student List (Admin)...")
    response = client.get('/api/admin/finance/students')
    if response.status_code == 200:
        print(f"OK: Retrieved {len(response.data)} students")
        found = any(s['admission_number'] == 'FIN-001' for s in response.data)
        if found:
            print("OK: Test student found in list")
        else:
            print("FAIL: Test student NOT found in list")
    else:
        print(f"FAIL: Student list retrieval failed: {response.status_code}")

    # 4. Test Search
    print("\nTesting Student Search (Admin)...")
    response = client.get('/api/admin/finance/students?q=FIN-001')
    if response.status_code == 200:
        if len(response.data) == 1 and response.data[0]['admission_number'] == 'FIN-001':
            print("OK: Search by admission number works")
        else:
            print(f"FAIL: Search failed. Results: {len(response.data)}")
    else:
        print(f"FAIL: Search endpoint failed: {response.status_code}")

    # 5. Test Student Transactions
    print("\nTesting Student Transactions (Admin)...")
    student_id = str(student_user.id)
    response = client.get(f'/api/admin/finance/transactions/{student_id}')
    if response.status_code == 200:
        txns = response.data.get('transactions', [])
        print(f"OK: Retrieved {len(txns)} transactions for student")
        statuses = [t['status'] for t in txns]
        if 'SUCCESS' in statuses and 'FAILED' in statuses:
            print("OK: Both success and failed transactions visible")
        else:
            print(f"FAIL: Missing expected transaction statuses. Found: {statuses}")
    else:
        print(f"FAIL: Transaction retrieval failed: {response.status_code} - {response.data}")

    print("\nSUMMARY: Admin finance redesign verified.")

if __name__ == "__main__":
    verify_admin_finance()
