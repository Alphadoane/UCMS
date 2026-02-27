
import os
import django
import sys

sys.path.append(os.getcwd())
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.contrib.auth import get_user_model
from student_api.core.serializers import UserProfileSerializer

User = get_user_model()

try:
    u = User.objects.get(email="peter.pan@kcau.ac.ke")
    print(f"User: {u.email}")
    print(f"Is Staff: {u.is_staff}")
    
    serializer = UserProfileSerializer(u)
    print("Serialized Data:")
    print(serializer.data)

except Exception as e:
    print(f"Error: {e}")
