import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.contrib.auth import get_user_model
User = get_user_model()

email = "admin@kcau.ac.ke"
password = "password123"

try:
    if not User.objects.filter(email=email).exists():
        print(f"Creating superuser: {email}")
        User.objects.create_superuser(email=email, password=password)
    else:
        print(f"Superuser {email} already exists. Resetting password.")
        u = User.objects.get(email=email)
        u.set_password(password)
        u.save()

    print(f"Superuser created/updated successfully.")
    print(f"Email: {email}")
    print(f"Password: {password}")
except Exception as e:
    print(f"Error: {e}")
