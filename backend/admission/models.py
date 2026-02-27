from django.db import models
from django.utils import timezone
import uuid
from student_api.academics.models import Program, Faculty, Department

class AdmissionApplication(models.Model):
    # Phase Status Constants
    PHASE_DRAFT = 'DRAFT'
    PHASE_APPLIED = 'APPLIED'
    PHASE_VERIFIED = 'VERIFIED'
    PHASE_SELECTED = 'SELECTED'
    PHASE_OFFERED = 'OFFERED'
    PHASE_ENROLLED = 'ENROLLED'
    PHASE_REJECTED = 'REJECTED'
    
    PHASE_CHOICES = [
        (PHASE_DRAFT, 'Draft'),
        (PHASE_APPLIED, 'Applied'),
        (PHASE_VERIFIED, 'Verified'),
        (PHASE_SELECTED, 'Selected'),
        (PHASE_OFFERED, 'Offered'),
        (PHASE_ENROLLED, 'Enrolled'),
        (PHASE_REJECTED, 'Rejected'),
    ]

    # Application Status
    STATUS_PENDING = 'PENDING'
    STATUS_APPROVED = 'APPROVED' # Generic approval, specific phase tracks progress
    STATUS_DENIED = 'DENIED'
    
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    application_date = models.DateTimeField(default=timezone.now)
    application_id = models.CharField(max_length=20, unique=True, blank=True) # e.g., APP/2026/0001
    application_id = models.CharField(max_length=20, unique=True, blank=True) # e.g., APP/2026/0001
    current_phase = models.CharField(max_length=20, choices=PHASE_CHOICES, default=PHASE_DRAFT, db_index=True)
    rejection_reason = models.TextField(blank=True, null=True)
    
    # 1. Personal Identity (The "Seed" of Identity)
    first_name = models.CharField(max_length=50)
    last_name = models.CharField(max_length=50)
    middle_name = models.CharField(max_length=50, blank=True)
    
    national_id = models.CharField(max_length=20, unique=True) # Critical unique identifier
    dob = models.DateField()
    gender = models.CharField(max_length=10, choices=[('M', 'Male'), ('F', 'Female'), ('O', 'Other')])
    nationality = models.CharField(max_length=50, default='Kenyan')
    
    email = models.EmailField(unique=True, blank=True, null=True) # Generated at enrollment
    phone = models.CharField(max_length=20)
    address = models.TextField()
    
    # 2. Academic History
    previous_institution = models.CharField(max_length=100)
    index_number = models.CharField(max_length=20) # KCSE Index
    mean_grade = models.CharField(max_length=5) # e.g., B+
    
    # 3. Programme Selection
    program_choice = models.ForeignKey(Program, on_delete=models.PROTECT) # Don't delete program if apps exist
    intake = models.CharField(max_length=20) # e.g., SEPT-2026
    mode_of_study = models.CharField(max_length=20, choices=[('FULL_TIME', 'Full Time'), ('PART_TIME', 'Part Time')])
    
    # 4. Financial / Admin
    sponsor_type = models.CharField(max_length=20, choices=[('SELF', 'Self Sponsored'), ('GOVT', 'Government'), ('HELB', 'HELB')], default='SELF')
    
    def get_required_document_types(self):
        """
        Returns a list of required document types based on applicant details and program.
        """
        # Core Mandatory
        required = [
            AdmissionDocument.DOC_TYPE_ID,
            AdmissionDocument.DOC_TYPE_PHOTO,
            AdmissionDocument.DOC_TYPE_MEDICAL,
        ]
        
        # International
        if self.nationality.lower() != 'kenyan':
            required.extend([
                AdmissionDocument.DOC_TYPE_PASSPORT,
                AdmissionDocument.DOC_TYPE_VISA,
                AdmissionDocument.DOC_TYPE_EQUIVALENCE,
            ])
            
        # Program based
        if self.program_choice.category.lower() == 'undergraduate':
            required.extend([
                AdmissionDocument.DOC_TYPE_RESULT_SLIP,
                AdmissionDocument.DOC_TYPE_LEAVING_CERT,
            ])
        elif self.program_choice.category.lower() == 'diploma':
            required.extend([
                AdmissionDocument.DOC_TYPE_RESULT_SLIP,
            ])
        elif self.program_choice.category.lower() in ['masters', 'doctoral', 'postgraduate']:
            required.extend([
                AdmissionDocument.DOC_TYPE_TRANSCRIPT,
            ])
            
        return list(set(required)) # Deduplicate if needed

    @property
    def missing_documents(self):
        uploaded_types = set(self.documents.values_list('document_type', flat=True))
        required = set(self.get_required_document_types())
        return list(required - uploaded_types)

    def save(self, *args, **kwargs):
        if not self.application_id:
            # Simple auto-generation logic
            count = AdmissionApplication.objects.count() + 1
            self.application_id = f"APP/2026/{count:04d}"
        super().save(*args, **kwargs)

    def __str__(self):
        return f"{self.application_id} - {self.first_name} {self.last_name}"
        
    class Meta:
        db_table = 'admission_applications'
        ordering = ['-application_date']


class AdmissionDocument(models.Model):
    DOC_TYPE_ID = 'NATIONAL_ID'
    DOC_TYPE_Birth_CERT = 'BIRTH_CERT'
    DOC_TYPE_RESULT_SLIP = 'RESULT_SLIP'
    DOC_TYPE_LEAVING_CERT = 'LEAVING_CERT'
    DOC_TYPE_TRANSCRIPT = 'TRANSCRIPT'
    DOC_TYPE_PHOTO = 'PASSPORT_PHOTO'
    DOC_TYPE_MEDICAL = 'MEDICAL_REPORT'
    DOC_TYPE_PASSPORT = 'PASSPORT'
    DOC_TYPE_VISA = 'STUDENT_VISA'
    DOC_TYPE_EQUIVALENCE = 'EQUIVALENCE_LETTER'
    
    DOC_CHOICES = [
        (DOC_TYPE_ID, 'National ID / Passport'),
        (DOC_TYPE_Birth_CERT, 'Birth Certificate'),
        (DOC_TYPE_RESULT_SLIP, 'KCSE Result Slip'),
        (DOC_TYPE_LEAVING_CERT, 'School Leaving Certificate'),
        (DOC_TYPE_TRANSCRIPT, 'Academic Transcripts'),
        (DOC_TYPE_PHOTO, 'Passport Photo'),
        (DOC_TYPE_MEDICAL, 'Medical Examination Report'),
        (DOC_TYPE_PASSPORT, 'Valid International Passport'),
        (DOC_TYPE_VISA, 'Student Visa'),
        (DOC_TYPE_EQUIVALENCE, 'Equivalence Letter'),
    ]
    
    application = models.ForeignKey(AdmissionApplication, related_name='documents', on_delete=models.CASCADE)
    document_type = models.CharField(max_length=20, choices=DOC_CHOICES)
    file = models.FileField(upload_to='admission_docs/%Y/%m/')
    uploaded_at = models.DateTimeField(auto_now_add=True)
    is_verified = models.BooleanField(default=False)
    verified_by = models.CharField(max_length=100, blank=True, null=True) # Store Admin email/ID
    rejection_reason = models.TextField(blank=True)
    
    class Meta:
        db_table = 'admission_documents'
