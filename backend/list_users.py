import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.contrib.auth import get_user_model
User = get_user_model()

print("---User List---")
for u in User.objects.all():
    role = "Student"
    if u.is_superuser:
        role = "Admin"
    elif u.is_staff:
        role = "Staff"
    print(f"Email: {u.email} | Name: {u.first_name} {u.last_name} | Role: {role}")
