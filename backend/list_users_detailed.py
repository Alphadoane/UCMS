import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from django.contrib.auth import get_user_model
from student_api.core.models import UserRole

User = get_user_model()
users = User.objects.all().order_by('email')

print(f"{'EMAIL':<35} | {'ROLES':<20} | {'NAME'}")
print("-" * 75)

for u in users:
    roles = ", ".join([ur.role.name for ur in UserRole.objects.filter(user=u)])
    name = f"{u.first_name} {u.last_name}"
    print(f"{u.email:<35} | {roles:<20} | {name}")
