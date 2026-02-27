import os
import django
import json

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from rest_framework_simplejwt.tokens import RefreshToken
from student_api.academics.models import Student
from django.test.client import Client

def test_api_calls():
    # Use existing student '2500001'
    try:
        student = Student.objects.get(admission_number='2500001')
        user = student.user
        
        # Authenticate
        client = Client()
        refresh = RefreshToken.for_user(user)
        token = str(refresh.access_token)
        
        headers = {'HTTP_AUTHORIZATION': f'Bearer {token}'}
        
        # Call finance_balance
        response = client.get('/api/finance/view-balance', **headers)
        print(f"Finance Balance Status: {response.status_code}")
        print(f"Finance Balance Response: {response.content.decode()}")
        
        # Call finance_statement
        response = client.get('/api/finance/receipts', **headers)
        print(f"Finance Statement Status: {response.status_code}")
        print(f"Content-Type: {response.headers.get('Content-Type')}")
        print(f"Body Length: {len(response.content)}")
        print(f"Finance Statement Response Body: {response.content.decode()}")
        
        # Call agora-token
        response = client.get('/api/virtual/agora-token?channelName=TestChannel&uid=123', **headers)
        print(f"Agora Token Status: {response.status_code}")
        print(f"Agora Token Response: {response.content.decode()}")
        
    except Student.DoesNotExist:
        print("Student 2500001 not found.")

if __name__ == "__main__":
    test_api_calls()
