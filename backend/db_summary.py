import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from django.apps import apps

print(f"{'APP':<15} | {'MODEL':<25} | {'COUNT'}")
print("-" * 50)

# List of system apps to exclude to reduce noise
exclude_apps = ['admin', 'auth', 'contenttypes', 'sessions', 'messages', 'staticfiles']

for model in apps.get_models():
    app_label = model._meta.app_label
    if app_label not in exclude_apps:
        try:
            count = model.objects.count()
            print(f"{app_label:<15} | {model._meta.object_name:<25} | {count}")
        except Exception as e:
             print(f"{app_label:<15} | {model._meta.object_name:<25} | Error: {e}")
