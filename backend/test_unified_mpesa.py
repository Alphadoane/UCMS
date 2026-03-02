import os
import django
import json
from decimal import Decimal

# Set up Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
django.setup()

from django.test import Client
from student_api.finance.models import Payment, FeeAccount
from student_api.academics.models import Student
from django.contrib.auth import get_user_model

User = get_user_model()

def test_mpesa_flow():
    print("--- Testing Unified M-Pesa Flow ---")
    client = Client()
    
    # 1. Setup Test Data
    user = User.objects.first() # Use any existing user
    if not user:
        print("Error: No user found in DB to test with.")
        return
        
    student = Student.objects.get(user=user)
    student.mpesa_phone = "0712345678"
    student.save()
    
    # Ensure FeeAccount exists
    account, _ = FeeAccount.objects.get_or_create(student=student)
    initial_balance = account.balance
    print(f"Initial Balance: {initial_balance}")

    # 2. Setup Factory
    from rest_framework.test import APIRequestFactory, force_authenticate
    factory = APIRequestFactory()

    # 3. Simulate Successful STK Push (Manual Record Creation)
    import uuid
    checkout_id = f"TEST_{uuid.uuid4().hex[:8]}"
    Payment.objects.create(
        student=student,
        amount=Decimal("100.00"),
        payment_method='MPESA',
        payment_rail='MPESA',
        status='PENDING',
        checkout_request_id=checkout_id,
        provider_reference=checkout_id
    )
    print(f"Manually created PENDING payment for CheckoutID: {checkout_id}")

    # 4. Simulate Callback
    callback_payload = {
        "Body": {
            "stkCallback": {
                "MerchantRequestID": "12345",
                "CheckoutRequestID": checkout_id,
                "ResultCode": 0,
                "ResultDesc": "The service request is processed successfully.",
                "CallbackMetadata": {
                    "Item": [
                        {"Name": "Amount", "Value": 100.00},
                        {"Name": "MpesaReceiptNumber", "Value": "TEST_RECEIPT_123"},
                        {"Name": "TransactionDate", "Value": 20260216124500},
                        {"Name": "PhoneNumber", "Value": 254712345678}
                    ]
                }
            }
        }
    }
    
    from student_api.finance.views import mpesa_callback
    callback_request = factory.post('/api/finance/mpesa/callback', callback_payload, format='json')
    print(f"Sending Callback for CheckoutID: {checkout_id}")
    callback_response = mpesa_callback(callback_request)
    
    if callback_response.status_code == 200:
        print("Callback Processed Successfully.")
        
        # 5. Verify Payment Record & Balance
        payment = Payment.objects.get(checkout_request_id=checkout_id)
        print(f"Payment Status: {payment.status}")
        print(f"Payment Receipt: {payment.mpesa_receipt_number}")
        
        account.refresh_from_db()
        print(f"Final Balance: {account.balance}")
        
        if payment.status == 'SUCCESS' and account.balance == initial_balance - Decimal('100.00'):
            print("TEST PASSED!")
        else:
            print("TEST FAILED: Balance or status not updated correctly.")
    else:
        print(f"Callback Failed: {callback_response.data}")

if __name__ == "__main__":
    test_mpesa_flow()
