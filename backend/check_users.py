import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.core.models import User

users = User.objects.all()
print(f"Total users: {users.count()}")
for u in users:
    print(f"User: {u.email} (Active: {u.is_active}, Staff: {u.is_staff}, Superuser: {u.is_superuser})")
