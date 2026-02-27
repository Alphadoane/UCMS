import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.core.models import User

try:
    u = User.objects.get(email='admin@kca.ac.ke')
    u.set_password('admin')
    u.save()
    print("Password for admin@kca.ac.ke set to 'admin'")
except User.DoesNotExist:
    print("User admin@kca.ac.ke not found!")
except Exception as e:
    print(f"Error: {e}")
