import os
import django

# Setup Django environment
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.academics.models import Program

print("--- Existing Programs ---")
for p in Program.objects.all():
    print(f"ID: {p.id} | Code: {p.code} | Name: {p.name}")
