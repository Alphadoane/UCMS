import os
import django

# Setup Django environment
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.core.models import User, Role, UserRole

try:
    lecturer_role = Role.objects.get(name='LECTURER')
    user_roles = UserRole.objects.filter(role=lecturer_role)
    
    print(f"Found {user_roles.count()} lecturers:")
    for ur in user_roles:
        user = ur.user
        print(f"Email: {user.email}, Name: {user.first_name} {user.last_name}")

except Role.DoesNotExist:
    # Fallback: check all users and their roles
    print("Role 'LECTURER' not found effectively. Listing all users with their roles:")
    for user in User.objects.all():
        roles = [ur.role.name for ur in UserRole.objects.filter(user=user)]
        print(f"Email: {user.email}, Roles: {roles}")

except Exception as e:
    print(f"An error occurred: {e}")
