import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.academics.models import Student
from student_api.finance.models import Payment

def audit():
    print('--- Student Payment Audit ---')
    students = Student.objects.all()
    for s in students:
        success_count = Payment.objects.filter(student=s, status='SUCCESS').count()
        pending_count = Payment.objects.filter(student=s, status='PENDING').count()
        failed_count = Payment.objects.filter(student=s, status='FAILED').count()
        print(f"User: {s.user.email}, Adm: {s.admission_number}, Success: {success_count}, Pending: {pending_count}, Failed: {failed_count}")

if __name__ == "__main__":
    audit()
