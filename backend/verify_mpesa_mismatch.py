import os
import django
import sys

# Set up Django environment
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from student_api.finance.models import Payment
from django.conf import settings

def verify():
    print("--- M-Pesa Implementation Verification ---")
    
    # 1. Check callback URL in settings/.env
    callback_url = getattr(settings, 'MPESA_CALLBACK_URL', 'NOT SET')
    print(f"MPESA_CALLBACK_URL: {callback_url}")
    
    # 2. Check for inconsistent payments
    # Payments created by Implementation 1 have provider_reference != None but checkout_request_id == None
    imp1_payments = Payment.objects.filter(provider_reference__isnull=False, checkout_request_id__isnull=True)
    print(f"Payments created by Imp 1 (provider_reference set, checkout_request_id null): {imp1_payments.count()}")
    for p in imp1_payments[:5]:
        print(f"  - ID: {p.id}, Ref: {p.provider_reference}, Status: {p.status}")

    # Payments created by Implementation 2 have checkout_request_id != None
    imp2_payments = Payment.objects.filter(checkout_request_id__isnull=False)
    print(f"Payments created by Imp 2 (checkout_request_id set): {imp2_payments.count()}")
    for p in imp2_payments[:5]:
        print(f"  - ID: {p.id}, CheckoutID: {p.checkout_request_id}, Status: {p.status}")

    # 3. Check for specific failure cases
    # Payments that are PENDING in Imp 1 but might have been failed by a missing callback
    pending_imp1 = Payment.objects.filter(provider_reference__isnull=False, checkout_request_id__isnull=True, status='PENDING')
    print(f"Pending Imp 1 payments (likely failed callback): {pending_imp1.count()}")

if __name__ == "__main__":
    verify()
