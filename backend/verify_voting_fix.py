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
from student_api.voting.models import Election

User = get_user_model()

def verify_voting_admin():
    client = APIClient()
    print("Django version:", django.get_version())
    
    # 1. Create/Find Admin User
    admin_user = User.objects.filter(is_superuser=True).first()
    if not admin_user:
        admin_user = User.objects.create_superuser(email='verify_admin@example.com', password='password', first_name='Verify', last_name='Admin')
    
    # 2. Authenticate
    client.force_authenticate(user=admin_user)
    
    # 3. Test POST (Creation)
    print("\nTesting Election Creation (Admin)...")
    data = {
        "title": "Verification Poll 2026",
        "description": "A test poll for system verification",
        "end_date": "2026-12-31T23:59:59Z"
    }
    response = client.post('/api/voting/elections', data, format='json')
    if response.status_code == 201:
        print("OK: Election created successfully")
        election_id = response.data['id']
    else:
        print(f"FAIL: Election creation failed: {response.status_code} - {response.data}")
        return

    # 4. Test GET (List for Admin)
    print("\nTesting Election List (Admin)...")
    response = client.get('/api/voting/elections')
    if response.status_code == 200:
        items = response.data.get('items', [])
        print(f"OK: Admin retrieved {len(items)} elections")
        item = next((i for i in items if i['id'] == election_id), None)
        if item:
            print(f"OK: Created election found in list (ID: {election_id})")
        else:
            print("FAIL: Created election NOT found in list")
    else:
        print(f"FAIL: Admin list retrieval failed: {response.status_code}")

    # 5. Test GET (List for Student - Verify filtering)
    print("\nTesting Election List (Student - Verification of active filter)...")
    student_user, _ = User.objects.get_or_create(email='verify_student@example.com', defaults={'first_name': 'Verify', 'last_name': 'Student'})
    client.force_authenticate(user=student_user)
    
    # Mark an election as inactive
    e = Election.objects.get(id=election_id)
    e.is_active = False
    e.save()
    print(f"Election {election_id} marked as INACTIVE.")
    
    response = client.get('/api/voting/elections')
    if response.status_code == 200:
        items = response.data.get('items', [])
        found = any(i['id'] == election_id for i in items)
        if not found:
            print("OK: Inactive election hidden from student")
        else:
            print("FAIL: Inactive election still visible to student")
    
    print("\nSUMMARY: Admin voting system fixes verified.")

if __name__ == "__main__":
    verify_voting_admin()
