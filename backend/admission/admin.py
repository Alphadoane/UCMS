from django.contrib import admin
from .models import AdmissionApplication, AdmissionDocument

class AdmissionDocumentInline(admin.TabularInline):
    model = AdmissionDocument
    extra = 0
    fields = ('document_type', 'file', 'is_verified', 'verified_by')
    readonly_fields = ('uploaded_at',)

@admin.register(AdmissionApplication)
class AdmissionApplicationAdmin(admin.ModelAdmin):
    list_display = ('application_id', 'full_name', 'program_choice', 'current_phase', 'application_date')
    list_filter = ('current_phase', 'intake', 'program_choice', 'gender', 'sponsor_type')
    search_fields = ('application_id', 'national_id', 'email', 'first_name', 'last_name')
    ordering = ('-application_date',)
    
    inlines = [AdmissionDocumentInline]
    
    fieldsets = (
        ('Application Status', {
            'fields': ('application_id', 'current_phase', 'rejection_reason')
        }),
        ('Personal Identity', {
            'fields': (('first_name', 'middle_name', 'last_name'), ('national_id', 'dob', 'gender'), ('nationality', 'email', 'phone'), 'address')
        }),
        ('Academic History', {
            'fields': ('previous_institution', 'index_number', 'mean_grade')
        }),
        ('Programme Selection', {
            'fields': ('program_choice', 'intake', 'mode_of_study')
        }),
        ('Admin/Financial', {
            'fields': ('sponsor_type', 'application_date')
        }),
    )
    readonly_fields = ('application_id', 'application_date')

    def full_name(self, obj):
        return f"{obj.first_name} {obj.last_name}"

@admin.register(AdmissionDocument)
class AdmissionDocumentAdmin(admin.ModelAdmin):
    list_display = ('document_type', 'application', 'is_verified', 'uploaded_at')
    list_filter = ('document_type', 'is_verified')
    search_fields = ('application__application_id', 'document_type')
