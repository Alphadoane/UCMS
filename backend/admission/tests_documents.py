from django.test import TestCase
from django.core.files.uploadedfile import SimpleUploadedFile
from student_api.academics.models import Program, Department, Faculty
from .models import AdmissionApplication, AdmissionDocument
from django.utils import timezone

class AdmissionDocumentTest(TestCase):
    def setUp(self):
        self.faculty = Faculty.objects.create(name="Science")
        self.department = Department.objects.create(faculty=self.faculty, name="CS")
        self.program_ug = Program.objects.create(
            department=self.department, 
            code="CS101", 
            name="BSc CS", 
            duration_years=4,
            category="Undergraduate"
        )
        self.program_dip = Program.objects.create(
            department=self.department, 
            code="IT101", 
            name="Diploma IT", 
            duration_years=2,
            category="Diploma"
        )
        
    def test_mandatory_documents(self):
        """Test that mandatory documents are listed for all."""
        app = AdmissionApplication.objects.create(
            first_name="John", last_name="Doe", 
            national_id="12345678", dob="2000-01-01", 
            email="john@example.com", 
            program_choice=self.program_ug,
            nationality="Kenyan"
        )
        self.assertEqual(app.current_phase, AdmissionApplication.PHASE_DRAFT)
        
        required = app.get_required_document_types()
        self.assertIn(AdmissionDocument.DOC_TYPE_ID, required)
        self.assertIn(AdmissionDocument.DOC_TYPE_PHOTO, required)
        self.assertIn(AdmissionDocument.DOC_TYPE_MEDICAL, required)
        
    def test_undergraduate_requirements(self):
        """Test UG specific requirements."""
        app = AdmissionApplication.objects.create(
            first_name="Jane", last_name="Doe", 
            national_id="87654321", dob="2000-01-01", 
            email="jane@example.com", 
            program_choice=self.program_ug,
            nationality="Kenyan"
        )
        required = app.get_required_document_types()
        self.assertIn(AdmissionDocument.DOC_TYPE_RESULT_SLIP, required)
        self.assertIn(AdmissionDocument.DOC_TYPE_LEAVING_CERT, required)
        
    def test_international_requirements(self):
        """Test International specific requirements."""
        app = AdmissionApplication.objects.create(
            first_name="Intl", last_name="Student", 
            national_id="INT123", dob="2000-01-01", 
            email="intl@example.com", 
            program_choice=self.program_dip,
            nationality="Ugandan"
        )
        required = app.get_required_document_types()
        self.assertIn(AdmissionDocument.DOC_TYPE_PASSPORT, required)
        self.assertIn(AdmissionDocument.DOC_TYPE_VISA, required)
        self.assertIn(AdmissionDocument.DOC_TYPE_EQUIVALENCE, required)
        
    def test_missing_documents_property(self):
        """Test calculation of missing documents."""
        app = AdmissionApplication.objects.create(
            first_name="Mark", last_name="Missing", 
            national_id="999999", dob="2000-01-01", 
            email="mark@example.com", 
            program_choice=self.program_ug,
            nationality="Kenyan"
        )
        
        # Upload ID
        AdmissionDocument.objects.create(
            application=app,
            document_type=AdmissionDocument.DOC_TYPE_ID,
            file=SimpleUploadedFile("id.jpg", b"content", content_type="image/jpeg")
        )
        
        missing = app.missing_documents
        self.assertNotIn(AdmissionDocument.DOC_TYPE_ID, missing)
        self.assertIn(AdmissionDocument.DOC_TYPE_PHOTO, missing)
