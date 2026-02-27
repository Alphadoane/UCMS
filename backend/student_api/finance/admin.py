from django.contrib import admin
from .models import FeeAccount, Invoice, Payment

@admin.register(FeeAccount)
class FeeAccountAdmin(admin.ModelAdmin):
    list_display = ('student', 'balance')
    search_fields = ('student__admission_number', 'student__user__email')

@admin.register(Invoice)
class InvoiceAdmin(admin.ModelAdmin):
    list_display = ('student', 'semester', 'amount', 'issued_at')
    list_filter = ('semester', 'issued_at')
    search_fields = ('student__admission_number',)

@admin.register(Payment)
class PaymentAdmin(admin.ModelAdmin):
    list_display = ('student', 'amount', 'payment_method', 'provider_reference', 'status', 'paid_at')
    list_filter = ('payment_method', 'status', 'paid_at')
    search_fields = ('student__admission_number', 'provider_reference')
