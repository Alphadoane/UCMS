from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from .models import Payment, FeeAccount
from student_api.academics.models import Student
from .mpesa import MpesaClient
import logging
import json
from decimal import Decimal

logger = logging.getLogger(__name__)

def format_phone_number(phone):
    """Formats phone number to 254xxxxxxxxx"""
    if phone.startswith('0'):
        return '254' + phone[1:]
    elif phone.startswith('254'):
        return phone
    return phone # fallback

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def initiate_stk_push(request):
    try:
        student = Student.objects.get(user=request.user)
    except Student.DoesNotExist:
        return Response({"error": "Student profile not found"}, status=404)

    # Support both Shared app (phoneNumber) and Native app (phone_number)
    phone = request.data.get('phoneNumber') or request.data.get('phone_number')
    amount = request.data.get('amount')

    if not phone or not amount:
        return Response({"error": "Phone number and amount are required"}, status=400)

    formatted_phone = format_phone_number(str(phone))
    client = MpesaClient()
    
    # Account reference is Admission Number (Truncate if needed as M-Pesa has limit)
    account_reference = student.admission_number 
    logger.info(f"Initiating STK Push for Reg No: {account_reference} Phone: {formatted_phone} Amount: {amount}")
    
    try:
        # Initiate STK Push
        # int(float(amount)) handles both "100" and "100.0" strings
        response = client.stk_push(formatted_phone, int(float(amount)), account_reference, "School Fee Payment")
        
        if response and response.get('ResponseCode') == '0':
            # Save pending payment
            Payment.objects.create(
                student=student,
                amount=amount,
                payment_method='MPESA',
                payment_rail='MPESA',
                phone_number=formatted_phone,
                status='PENDING',
                checkout_request_id=response.get('CheckoutRequestID'),
                merchant_request_id=response.get('MerchantRequestID'),
                provider_reference=response.get('CheckoutRequestID') # Also set provider_reference for logic that uses it
            )
            return Response({
                "status": "Success", 
                "message": "STK Push initiated. Check your phone to complete payment.",
                "checkout_request_id": response.get('CheckoutRequestID'),
                "merchant_request_id": response.get('MerchantRequestID')
            })
        else:
            error_msg = response.get('customerMessage', 'Failed to initiate M-Pesa payment') if response else "Unknown error"
            return Response({"error": error_msg, "mpesa_response": response}, status=400)
            
    except Exception as e:
        import traceback
        logger.error(f"STK Push Error: {str(e)}")
        logger.error(traceback.format_exc())
        return Response({"error": "Internal server error during payment initiation"}, status=500)


@api_view(['POST'])
@permission_classes([AllowAny])
def mpesa_callback(request):
    """
    Handles M-Pesa Callback
    """
    logger.info(f"M-Pesa Callback received: {request.data}")
    
    try:
        body = request.data.get('Body', {})
        stk_callback = body.get('stkCallback', {})
        
        merchant_request_id = stk_callback.get('MerchantRequestID')
        checkout_request_id = stk_callback.get('CheckoutRequestID')
        result_code = stk_callback.get('ResultCode')
        result_desc = stk_callback.get('ResultDesc')
        
        try:
            payment = Payment.objects.get(checkout_request_id=checkout_request_id)
        except Payment.DoesNotExist:
            logger.error(f"Payment not found for CheckoutRequestID: {checkout_request_id}")
            return Response({"status": "Payment not found"}, status=404)
            
        if result_code == 0:
            # Payment Success
            meta_items = stk_callback.get('CallbackMetadata', {}).get('Item', [])
            receipt_number = next((item.get('Value') for item in meta_items if item.get('Name') == 'MpesaReceiptNumber'), None)
            
            payment.status = 'SUCCESS'
            payment.mpesa_receipt_number = receipt_number
            payment.metadata = json.dumps(stk_callback)
            payment.save()
            
            # Update Student Fee Account Balance
            # Assuming logic: payment reduces balance (credit)
            fee_account, created = FeeAccount.objects.get_or_create(student=payment.student)
            fee_account.balance = fee_account.balance - Decimal(payment.amount)
            fee_account.save()
            
            logger.info(f"Payment confirmed: {receipt_number} for {payment.student.admission_number}")
        else:
            # Payment Failed / Cancelled
            payment.status = 'FAILED'
            payment.metadata = json.dumps(stk_callback)
            payment.save()
            logger.warning(f"Payment failed: {result_desc}")
            
        return Response({"status": "Callback processed"})
        
    except Exception as e:
        logger.error(f"Error processing callback: {str(e)}")
        return Response({"error": "Callback processing failed"}, status=500)
