import os
import django

# Setup Django environment
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.core.models import User, Role, UserRole

try:
    # Ensure Role Exists
    lecturer_role, created = Role.objects.get_or_create(name='LECTURER')
    if created:
        print("Created new role: LECTURER")
    else:
        print("Role LECTURER already exists")

    # Create User
    email = "lecturer@kcau.ac.ke"
    password = "password123"
    
    user, created = User.objects.get_or_create(email=email)
    if created:
        user.first_name = "Jane"
        user.last_name = "Doe"
        user.set_password(password)
        user.save()
        print(f"Created new user: {email}")
    else:
        print(f"User {email} already exists. Resetting password.")
        user.set_password(password)
        user.save()

    # Assign Role
    UserRole.objects.get_or_create(user=user, role=lecturer_role)
    print(f"Assigned LECTURER role to {email}")

    print("\n--- CREDENTIALS ---")
    print(f"Email: {email}")
    print(f"Password: {password}")

except Exception as e:
    print(f"An error occurred: {e}")
