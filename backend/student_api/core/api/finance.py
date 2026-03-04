from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated, AllowAny, IsAdminUser
from django.db import transaction
from django.db.models import Sum
from student_api.academics.models import Student
from student_api.finance.models import FeeAccount, Invoice, Payment
from student_api.finance.paystack import PaystackClient
from student_api.finance.mpesa import MpesaClient
import logging
import json
import uuid

logger = logging.getLogger(__name__)

def process_settlement(payment, gateway_ref, metadata=None):
    """
    Helper function to process a successful payment settlement.
    Ensures idempotency by checking payment status before updating.
    """
    if payment.status == 'SUCCESS':
        logger.info(f"Payment {payment.id} already settled.")
        return True

    try:
        with transaction.atomic():
            payment.status = 'SUCCESS'
            payment.metadata = payment.metadata or {}
            if metadata:
                payment.metadata.update(metadata)
            if gateway_ref and payment.payment_rail == 'MPESA':
                payment.mpesa_receipt_number = gateway_ref
            
            payment.metadata['gateway_settlement_ref'] = gateway_ref
            payment.save()

            # Update Student Balance (Credit/Reduce)
            account, created = FeeAccount.objects.get_or_create(student=payment.student)
            account.balance -= payment.amount
            account.save()

            logger.info(f"Payment SUCCESS for {payment.student.admission_number}. Amount: {payment.amount}. Rail: {payment.payment_rail}")
            return True
    except Exception as e:
        logger.error(f"Error processing settlement for payment {payment.id}: {str(e)}")
        return False

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def finance_balance(request):
    try:
        student = Student.objects.get(user=request.user)
        account, created = FeeAccount.objects.get_or_create(student=student)
        
        # Calculate real totals
        total_billed = Invoice.objects.filter(student=student).aggregate(total=Sum('amount'))['total'] or 0.0
        total_paid = Payment.objects.filter(student=student, status='SUCCESS').aggregate(total=Sum('amount'))['total'] or 0.0
        
        return Response({
            "balance": float(account.balance),
            "total_billed": float(total_billed),
            "total_paid": float(total_paid),
            "currency": "KES",
            "student_id": student.admission_number
        })
    except Student.DoesNotExist:
        return Response({"detail": "Student profile not found"}, status=404)
    except Exception as e:
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def finance_stk_push(request):
    try:
        student = Student.objects.get(user=request.user)
        
        # Determine if payload is from Android Native (StkPushRequest) or Shared KMP (MpesaPaymentRequest)
        payment_type = request.data.get('payment_type')
        amount_val = request.data.get('amount')
        
        # KMP client sends 'phoneNumber' (camelCase), native may send 'phone_number' (snake_case)
        phone_val = (
            request.data.get('phoneNumber')
            or request.data.get('phone_number')
        )
        
        account, created = FeeAccount.objects.get_or_create(student=student)
        
        if payment_type:
            # Native Android payload processing (StkPushRequest)
            if payment_type == 'FULL':
                amount = float(account.balance)
            else:
                if not amount_val:
                    return Response({"detail": "Amount is required for partial payment"}, status=400)
                amount = float(amount_val)
        else:
            # Shared KMP Payload processing (MpesaPaymentRequest)
            if not amount_val:
                return Response({"detail": "Amount is required for payment"}, status=400)
            amount = float(amount_val)
            
        # Only reject truly invalid amounts (zero or negative). 
        # Do NOT reject amounts that exceed balance — the student may be overpaying or balance may be stale.
        if amount < 1:
            return Response({"detail": "Minimum payment is KES 1"}, status=400)

        # Ensure we have a valid phone number (prefer request value, fallback to profile)
        phone = phone_val or student.mpesa_phone
        if not phone:
            return Response(
                {"detail": "M-Pesa phone number not provided. Please enter your phone number or update your profile."},
                status=400
            )
        
        # Clean phone number to 254XXXXXXXXX format
        phone = str(phone).strip()
        if phone.startswith('0'):
            phone = '254' + phone[1:]
        elif phone.startswith('+'):
            phone = phone[1:]
        
        client = MpesaClient()
        
        # Check if M-Pesa is configured (not placeholder)
        if client.consumer_key in ('PLACEHOLDER_KEY', '') or client.consumer_secret in ('PLACEHOLDER_SECRET', ''):
            logger.warning("M-Pesa credentials are not configured. Using sandbox demo mode.")
            # Return a useful error for development instead of silent 400
            return Response({
                "detail": "M-Pesa is not yet configured on the server. Please contact admin.",
                "status": "not_configured",
                "hint": "Set MPESA_CONSUMER_KEY, MPESA_CONSUMER_SECRET, MPESA_PASSKEY in environment."
            }, status=503)
        
        response = client.stk_push(
            phone=phone,
            amount=amount,
            account_reference=student.admission_number,
            transaction_desc=f"Fee Payment - {student.admission_number}"
        )
        
        if response and response.get('ResponseCode') == '0':
            # Create pending payment record
            Payment.objects.create(
                student=student,
                amount=amount,
                payment_method="M-Pesa",
                payment_rail="MPESA",
                provider_reference=response.get('CheckoutRequestID'),
                status='PENDING',
                metadata={"merchant_request_id": response.get('MerchantRequestID')}
            )
            return Response({
                "status": "pending",
                "message": "STK Push sent. Check your phone to complete payment.",
                "checkout_request_id": response.get('CheckoutRequestID'),
                "merchant_request_id": response.get('MerchantRequestID')
            })
        else:
            mpesa_error = response.get('errorMessage', 'Unknown error') if response else 'No response from M-Pesa'
            logger.error(f"STK Push failed. Response: {response}")
            return Response({
                "detail": f"M-Pesa STK Push failed: {mpesa_error}",
                "mpesa_error": response
            }, status=400)
            
    except Student.DoesNotExist:
        return Response({"detail": "Student profile not found"}, status=404)
    except Exception as e:
        logger.exception("STK Push error")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def finance_paystack_initialize(request):
    """
    Initialize a Paystack transaction for Bank Transfer (PesaLink).
    """
    try:
        student = Student.objects.get(user=request.user)
        payment_type = request.data.get('payment_type', 'FULL')
        amount_val = request.data.get('amount')
        
        account, created = FeeAccount.objects.get_or_create(student=student)
        amount = account.balance if payment_type == 'FULL' else float(amount_val or 0)
        
        if amount <= 0:
            return Response({"detail": "Invalid amount"}, status=400)

        reference = f"PAY_{uuid.uuid4().hex[:10]}"
        client = PaystackClient()
        init_data = client.initialize_transaction(
            email=request.user.email,
            amount=amount,
            reference=reference
        )
        
        if init_data:
            Payment.objects.create(
                student=student,
                amount=amount,
                payment_method="Bank Transfer",
                payment_rail="BANK_PESALINK",
                provider_reference=reference,
                status='PENDING'
            )
            return Response(init_data)
        else:
            return Response({"detail": "Failed to initialize bank payment"}, status=400)
            
    except Exception as e:
        logger.exception("Paystack init error")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([AllowAny])
def finance_paystack_webhook(request):
    """
    Handle Paystack webhook for real-time settlement.
    """
    try:
        client = PaystackClient()
        signature = request.headers.get('x-paystack-signature')
        if not signature:
            return Response({"status": "error", "message": "Missing signature"}, status=401)
        
        # Use raw data for signature verification
        if not client.verify_webhook_signature(request.body, signature):
            return Response({"status": "error", "message": "Invalid signature"}, status=401)

        payload = request.data
        event = payload.get('event')
        data = payload.get('data', {})
        
        if event == 'charge.success':
            reference = data.get('reference')
            try:
                payment = Payment.objects.get(provider_reference=reference)
                process_settlement(payment, data.get('id'), {"source": "paystack_webhook"})
            except Payment.DoesNotExist:
                logger.error(f"Payment intent not found for ref: {reference}")
                
        return Response({"status": "received"})
    except Exception as e:
        logger.exception("Paystack webhook error")
        return Response({"status": "error", "message": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([AllowAny]) # Safaricom callback
def finance_mpesa_callback(request):
    try:
        data = request.data
        stk_callback = data.get('Body', {}).get('stkCallback', {})
        result_code = stk_callback.get('ResultCode')
        checkout_request_id = stk_callback.get('CheckoutRequestID')
        
        try:
            payment = Payment.objects.get(provider_reference=checkout_request_id)
        except Payment.DoesNotExist:
            logger.error(f"Payment not found for checkout_request_id: {checkout_request_id}")
            return Response({"status": "error", "message": "Payment not found"}, status=400)
            
        if result_code == 0:
            # Success
            metadata_list = stk_callback.get('CallbackMetadata', {}).get('Item', [])
            receipt = ""
            for item in metadata_list:
                if item.get('Name') == 'MpesaReceiptNumber':
                    receipt = item.get('Value')
                    break
            
            process_settlement(payment, receipt, {"source": "mpesa_callback"})
        else:
            # Failed or Cancelled
            if payment.status == 'PENDING':
                payment.status = 'FAILED'
                payment.save()
            logger.warning(f"Payment FAILED for {payment.student.admission_number}. ResultCode: {result_code}")
            
        return Response({"ResultCode": 0, "ResultDesc": "Success"})
    except Exception as e:
        logger.exception("Callback processing error")
        return Response({"status": "error", "message": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def finance_statement(request):
    try:
        student = Student.objects.get(user=request.user)
        payments = Payment.objects.filter(student=student).order_by('-paid_at')
        transactions = []
        student_name = f"{student.user.first_name} {student.user.last_name}"
        for p in payments:
            receipt_ref = p.mpesa_receipt_number or p.provider_reference or "N/A"
            item = {
                "id": p.id,
                "student_name": student_name,
                "admission_number": student.admission_number,
                "amount": float(p.amount),
                "status": p.status,
                "payment_method": p.payment_method or "N/A",
                "method": p.payment_method or "N/A",  # Alias for native app
                "date": p.paid_at.strftime('%Y-%m-%d %H:%M:%S') if p.paid_at else "N/A",
                "reference": receipt_ref,
                "receipt": receipt_ref, # Alias for shared app
                "ref": receipt_ref, # Alias for native app screen logic
                "receipt_no": receipt_ref, # Alias for native app model
                "description": f"Fee Payment - {receipt_ref}" # Alias for native app model
            }
            transactions.append(item)
        
        return Response({
            "transactions": transactions,
            "receipts": transactions # Alias for native app
        })
    except Student.DoesNotExist:
        return Response({"detail": "Student profile not found"}, status=404)
    except Exception as e:
        import traceback
        logger.error(f"Error in finance_statement: {str(e)}")
        logger.error(traceback.format_exc())
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_finance_students(request):
    """
    Admin endpoint to list students with their balances and basic info.
    Supports searching via 'q' query param.
    """
    query = request.GET.get('q', '')
    students = Student.objects.all()
    
    if query:
        from django.db.models import Q
        students = students.filter(
            Q(user__first_name__icontains=query) | 
            Q(user__last_name__icontains=query) | 
            Q(admission_number__icontains=query)
        )

    data = []
    for student in students:
        account, _ = FeeAccount.objects.get_or_create(student=student)
        data.append({
            "id": str(student.user.id),
            "admission_number": student.admission_number,
            "full_name": f"{student.user.first_name} {student.user.last_name}",
            "balance": float(account.balance),
            "status": student.status
        })

    return Response(data)

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_student_transactions(request, student_id):
    """
    Admin endpoint to view all transactions for a specific student.
    """
    try:
        # student_id is user_id
        student = Student.objects.get(user_id=student_id)
        payments = Payment.objects.filter(student=student).order_by('-paid_at')
        
        transactions = []
        for p in payments:
            transactions.append({
                "id": p.id,
                "amount": float(p.amount),
                "status": p.status,
                "payment_method": p.payment_method,
                "payment_rail": p.payment_rail,
                "reference": p.mpesa_receipt_number or p.provider_reference or "N/A",
                "date": p.paid_at.strftime('%Y-%m-%d %H:%M:%S') if p.paid_at else "N/A",
                "phone": p.phone_number or "N/A"
            })

        return Response({
            "student_name": f"{student.user.first_name} {student.user.last_name}",
            "admission_number": student.admission_number,
            "transactions": transactions
        })
    except Student.DoesNotExist:
        return Response({"detail": "Student not found"}, status=404)
    except Exception as e:
        logger.error(f"Error fetching student transactions: {str(e)}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_all_transactions(request):
    """
    Admin endpoint to view all transactions (M-Pesa, Bank, etc.)
    """
    try:
        # Fetch all payments, ordered by newest first
        payments = Payment.objects.all().order_by('-paid_at')
        transactions = []
        for p in payments:
            # Get student info
            student_name = "Unknown"
            admission_no = "Unknown"
            try:
                # Use select_related in query to optimize if possible, but for now simple access
                # Payment.student is ForeignKey to Student
                # Student.user is OneToOne to User
                student = p.student
                admission_no = student.admission_number
                student_name = f"{student.user.first_name} {student.user.last_name}"
            except Exception:
                pass

            transactions.append({
                "id": p.id,
                "student_name": student_name,
                "admission_number": admission_no,
                "amount": float(p.amount),
                "status": p.status, # PENDING, SUCCESS, FAILED
                "reference": p.provider_reference or p.checkout_request_id or "N/A",
                "payment_method": p.payment_method,
                "date": p.paid_at.strftime('%Y-%m-%d %H:%M:%S') if p.paid_at else "N/A",
                "receipt": p.mpesa_receipt_number or "N/A"
            })
        
        return Response({"transactions": transactions})
    except Exception as e:
        logger.error(f"Admin Transaction Error: {str(e)}")
        return Response({"detail": str(e)}, status=500)
