from django.db import models
from student_api.academics.models import Semester, Student

# 6. Finance
class FeeAccount(models.Model):
    student = models.OneToOneField(Student, on_delete=models.CASCADE, primary_key=True)
    balance = models.DecimalField(max_digits=12, decimal_places=2, default=0.00)
    
    class Meta:
        db_table = 'fee_accounts'

class Invoice(models.Model):
    student = models.ForeignKey(Student, on_delete=models.CASCADE)
    semester = models.ForeignKey(Semester, on_delete=models.CASCADE)
    amount = models.DecimalField(max_digits=12, decimal_places=2)
    issued_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'invoices'

class Payment(models.Model):
    student = models.ForeignKey(Student, on_delete=models.CASCADE)
    amount = models.DecimalField(max_digits=12, decimal_places=2)
    payment_method = models.CharField(max_length=50)
    PAYMENT_RAIL_CHOICES = [
        ('MPESA', 'M-Pesa'),
        ('BANK_PESALINK', 'PesaLink (Bank Transfer)'),
        ('OTHER', 'Other'),
    ]
    payment_rail = models.CharField(max_length=20, choices=PAYMENT_RAIL_CHOICES, default='MPESA')
    provider_reference = models.CharField(max_length=100, unique=True, null=True, blank=True) # Gateway transaction ID
    metadata = models.JSONField(null=True, blank=True) # For additional data like bank name, logs, etc.
    
    PAYMENT_STATUS = [
        ('PENDING', 'Pending'),
        ('PROCESSING', 'Processing'),
        ('SUCCESS', 'Success'),
        ('FAILED', 'Failed'),
    ]
    status = models.CharField(max_length=20, choices=PAYMENT_STATUS, default='PENDING')
    
    # M-Pesa specific fields
    checkout_request_id = models.CharField(max_length=100, unique=True, null=True, blank=True)
    merchant_request_id = models.CharField(max_length=100, null=True, blank=True)
    phone_number = models.CharField(max_length=15, null=True, blank=True)
    mpesa_receipt_number = models.CharField(max_length=50, null=True, blank=True)
    
    paid_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'payments'
