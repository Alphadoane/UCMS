from rest_framework.decorators import api_view, permission_classes, parser_classes
from rest_framework.response import Response
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework import status
from django.shortcuts import get_object_or_404
from .models import AdmissionApplication, AdmissionDocument
from .serializers import AdmissionApplicationSerializer, AdmissionDocumentSerializer, ApplicationStatusSerializer, ProgramSerializer
from student_api.academics.models import Program
from django.utils import timezone
import logging

logger = logging.getLogger(__name__)

# --- Phase 1: Public Application ---

@api_view(["GET"])
@permission_classes([AllowAny])
def get_programs(request):
    """
    Public endpoint to list available programs.
    """
    programs = Program.objects.all()
    return Response(ProgramSerializer(programs, many=True).data)

@api_view(["POST"])
@permission_classes([AllowAny])
def apply_admission(request):
    """
    Public endpoint for new students to apply.
    """
    logger.info(f"DEBUG: apply_admission received data: {request.data}")
    serializer = AdmissionApplicationSerializer(data=request.data)
    if serializer.is_valid():
        app = serializer.save()
        logger.info(f"DEBUG: Application saved successfully: {app.application_id}")
        return Response({
            "message": "Application submitted successfully",
            "application_id": app.application_id,
            "tracking_id": app.id # For document uploads
        }, status=201)
    
    logger.error(f"DEBUG: apply_admission validation failed: {serializer.errors}")
    return Response(serializer.errors, status=400)

@api_view(["POST"])
@permission_classes([AllowAny])
@parser_classes([MultiPartParser, FormParser])
def upload_document(request, application_id):
    """
    Upload documents for a specific application ID (e.g. APP/...)
    """
    logger.info(f"DEBUG: upload_document called for app_id={application_id}")
    logger.info(f"DEBUG: request.data keys: {list(request.data.keys())}")
    logger.info(f"DEBUG: request.FILES keys: {list(request.FILES.keys())}")
    
    app = get_object_or_404(AdmissionApplication, application_id=application_id)
    
    try:
        # Check if files are attached
        if 'file' not in request.FILES:
            logger.warning(f"DEBUG: No 'file' in request.FILES. Available: {list(request.FILES.keys())}")
            return Response({"detail": "No file provided. Please ensure key name is 'file'"}, status=400)
    
        doc_type = request.data.get('document_type') or request.data.get('type')
        if not doc_type:
            logger.warning(f"DEBUG: Missing document_type or type in request.data: {request.data}")
            return Response({"detail": "Document type or type required"}, status=400)
            
        # [SEC-04] IDOR FIX: Verify ownership using National ID (simulated session)
        national_id = request.data.get('national_id')
        logger.info(f"DEBUG: Comparing national_id: received={national_id}, expected={app.national_id}")
        
        if not national_id or app.national_id != str(national_id):
            return Response({"detail": f"Verification failed: National ID {national_id} does not match application records."}, status=403)

        # Check if document already exists for this type
        existing_doc = AdmissionDocument.objects.filter(application=app, document_type=doc_type).first()
        if existing_doc:
            logger.info(f"DEBUG: Replacing existing document for {doc_type}")
            existing_doc.file = request.FILES['file']
            existing_doc.save()
            return Response(AdmissionDocumentSerializer(existing_doc).data, status=200)

        logger.info(f"DEBUG: Creating new document for {doc_type}")
        doc = AdmissionDocument.objects.create(
            application=app,
            document_type=doc_type,
            file=request.FILES['file']
        )
        return Response(AdmissionDocumentSerializer(doc).data, status=201)
    except Exception as e:
        logger.exception(f"Upload failed for {application_id}")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([AllowAny])
def submit_application(request, application_id):
    """
    Finalize submission (change phase to APPLIED). Public access.
    """
    app = get_object_or_404(AdmissionApplication, application_id=application_id)
    
    # Optional: Check if documents are uploaded
    if not app.documents.exists():
         return Response({"detail": "Please upload documents first"}, status=400)
         
    app.current_phase = AdmissionApplication.PHASE_APPLIED
    app.save()
    
    return Response({"message": "Application submitted successfully", "status": "APPLIED"})

@api_view(["GET"])
@permission_classes([AllowAny])
def check_status(request):
    """
    Check status by National ID or Application ID.
    """
    query = request.query_params.get('q')
    if not query:
        return Response({"detail": "Query parameter 'q' required"}, status=400)
        
    # [SEC-05] Privacy Leak FIX: Require email verification
    email_verification = request.query_params.get('verify_email')
    
    # Try finding by National ID first, then App ID
    app = AdmissionApplication.objects.filter(national_id=query).first()
    if not app:
        app = AdmissionApplication.objects.filter(application_id=query).first()
    if not app:
        # Also try internal UUID (id)
        try:
            app = AdmissionApplication.objects.filter(id=query).first()
        except:
            pass
        
    if app:
        # Check if email provided matches (Case insensitive)
        # If the app is in DRAFT phase, we might allow bypass if called from wizard?
        # But for security, if email is set, we should check it.
        # If email is NOT set (still in draft form), we can skip if matched by UUID/AppID?
        if app.current_phase != AdmissionApplication.PHASE_DRAFT:
            if not email_verification or app.email.lower() != email_verification.lower():
                return Response({"detail": "Verification failed. Email does not match records."}, status=403)
             
        return Response(ApplicationStatusSerializer(app).data)
    return Response({"detail": "Application not found"}, status=404)

# --- Phase 2-5: Admin Management ---

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_applications(request):
    if not request.user.is_staff: return Response(status=403)
    
    # Filter by phase if needed
    phase = request.query_params.get('phase')
    if phase:
        apps = AdmissionApplication.objects.filter(current_phase=phase)
    else:
        apps = AdmissionApplication.objects.exclude(current_phase=AdmissionApplication.PHASE_DRAFT)
        
    return Response(AdmissionApplicationSerializer(apps, many=True).data)

# --- Admin Program Management ---

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def admin_create_program(request):
    if not request.user.is_staff: return Response(status=403)
    serializer = ProgramSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)

@api_view(["PUT", "DELETE"])
@permission_classes([IsAuthenticated])
def admin_manage_program(request, program_id):
    if not request.user.is_staff: return Response(status=403)
    program = get_object_or_404(Program, id=program_id)
    
    if request.method == "PUT":
        serializer = ProgramSerializer(program, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=400)
        
    elif request.method == "DELETE":
        program.delete()
        return Response(status=204)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def verify_document(request, doc_id):
    if not request.user.is_staff: return Response(status=403)
    
    doc = get_object_or_404(AdmissionDocument, id=doc_id)
    action = request.data.get('action') # 'approve' or 'reject'
    reason = request.data.get('reason', '')
    
    if action == 'approve':
        doc.is_verified = True
        doc.rejection_reason = ""
        doc.verified_by = request.user.email
    elif action == 'reject':
        doc.is_verified = False
        doc.rejection_reason = reason
        doc.verified_by = request.user.email
    else:
        return Response({"detail": "Invalid action. Use 'approve' or 'reject'."}, status=400)
        
    doc.save()
    return Response(AdmissionDocumentSerializer(doc).data)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def update_phase(request, application_id):
    if not request.user.is_staff: return Response(status=403)
    
    app = get_object_or_404(AdmissionApplication, application_id=application_id)
    new_phase = request.data.get('phase')
    reason = request.data.get('reason')
    
    if new_phase not in dict(AdmissionApplication.PHASE_CHOICES):
        return Response({"detail": "Invalid phase"}, status=400)
        
    app.current_phase = new_phase
    if new_phase == AdmissionApplication.PHASE_REJECTED and reason:
        app.rejection_reason = reason
        
    app.save()
    return Response(ApplicationStatusSerializer(app).data)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def enroll_student(request, application_id):
    """
    Phase 5: Enrollment. identity Factory Conversion.
    """
    if not (request.user.is_staff or request.user.is_superuser): 
        return Response(status=403)
        
    app = get_object_or_404(AdmissionApplication, application_id=application_id)
    
    if app.current_phase != AdmissionApplication.PHASE_OFFERED:
         return Response({"detail": "Applicant must be in OFFERED phase to enroll"}, status=400)

    try:
        from student_api.core.services import EnrollmentService
        
        result = EnrollmentService.enroll_application(app)
        
        # 5. Finalize Application (if not handled in service, but service does logic)
        # Service logic above just returned credentials. We need to save app state if service didn't.
        # My service implementation didn't update app state. I should check that.
        # Wait, I wrote `EnrollmentService` to strictly do logic.
        # Let's double check `EnrollmentService` code I wrote. 
        # I didn't verify if I included app.save() in service. 
        # I checked my previous write_to_file for services.py. 
        # It DOES NOT update 'app.current_phase'. 
        # So I must do it here or update service.
        # Let's do it here for now to keep service pure-ish or update service later.
        
        app.current_phase = AdmissionApplication.PHASE_ENROLLED
        app.email = result['email']
        app.save()
        
        return Response({
            "message": "Student Enrolled Successfully",
            "reg_number": result['reg_number'],
            "email": result['email'],
            "temp_password": result['temp_password']
        })
        
    except Exception as e:
        logger.exception("Enrollment Failed")
        return Response({"detail": str(e)}, status=500)
