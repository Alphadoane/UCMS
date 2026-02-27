import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.finance.models import Payment, FeeAccount, Invoice
from student_api.academics.models import Student
from django.db.models import Sum

def run_diagnostics():
    admission_numbers = ['30/00001', '2500001']
    for adm in admission_numbers:
        try:
            student = Student.objects.get(admission_number=adm)
            print(f"\n--- Diagnostics for {adm} ({student.user.first_name} {student.user.last_name}) ---")
            
            # Simulate finance_balance response
            account, _ = FeeAccount.objects.get_or_create(student=student)
            total_billed = Invoice.objects.filter(student=student).aggregate(total=Sum('amount'))['total'] or 0.0
            total_paid = Payment.objects.filter(student=student, status='SUCCESS').aggregate(total=Sum('amount'))['total'] or 0.0
            balance_resp = {
                "balance": float(account.balance),
                "total_billed": float(total_billed),
                "total_paid": float(total_paid),
                "currency": "KES"
            }
            print(f"v1 finance_balance Simulate JSON: {balance_resp}")

            # Simulate finance_statement response (Updated format)
            payments = Payment.objects.filter(student=student, status='SUCCESS').order_by('-paid_at')
            student_name = f"{student.user.first_name} {student.user.last_name}"
            transactions = []
            for p in payments:
                ref = p.mpesa_receipt_number or p.provider_reference or "N/A"
                transactions.append({
                    "id": p.id,
                    "student_name": student_name,
                    "admission_number": student.admission_number,
                    "amount": float(p.amount),
                    "status": p.status,
                    "payment_method": p.payment_method,
                    "date": p.paid_at.strftime('%Y-%m-%d %H:%M:%S'),
                    "reference": ref,
                    "receipt": ref
                })
            print(f"v1 finance_statement Simulate JSON (count={len(transactions)}): {transactions[:1]}")
                
        except Student.DoesNotExist:
            print(f"Student {adm} not found.")

if __name__ == "__main__":
    run_diagnostics()
