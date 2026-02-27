import os
import django
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.auth_service import AuthService
from django.contrib.auth import get_user_model

try:
    print("Testing Auth...")
    username = "student1@kca.ac.ke"
    password = "password123"
    
    User = get_user_model()
    print(f"User Model: {User}")
    
    exists = User.objects.filter(email=username).exists()
    print(f"User exists: {exists}")
    
    if exists:
        u = User.objects.get(email=username)
        print(f"User pass hash: {u.password}")
        print(f"Check pass: {u.check_password(password)}")
    
    user = AuthService.authenticate_user(username, password)
    print(f"Auth Result: {user}")

except Exception as e:
    print(f"Error: {e}")
