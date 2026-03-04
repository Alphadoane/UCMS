from rest_framework.decorators import api_view, permission_classes, parser_classes
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.parsers import MultiPartParser, FormParser
from student_api.support.models import Complaint, ComplaintAttachment
from student_api.support.services import UCMSService
from student_api.core.serializers import ComplaintSerializer
from student_api.academics.models import Student, Course

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
@parser_classes([MultiPartParser, FormParser])
def support_tickets(request):
    """Handles both student submission and listed complaints (UCMS)"""
    user = request.user
    
    if request.method == "POST":
        try:
            student = Student.objects.get(user=user)
        except Student.DoesNotExist:
            return Response({"detail": "Only students can submit complaints"}, status=403)
            
        course_id = request.data.get('course_id')
        description = request.data.get('description')
        priority = request.data.get('priority', 'medium')
        
        if not description:
            return Response({"detail": "description is required"}, status=400)
            
        if not course_id:
            # Fallback to first course if not provided (for older clients)
            first_course = Course.objects.first()
            if not first_course:
                return Response({"detail": "No courses available in system"}, status=400)
            course = first_course
        else:
            try:
                course = Course.objects.get(id=course_id)
            except (Course.DoesNotExist, ValueError):
                return Response({"detail": "Invalid course_id"}, status=400)
            
        # Create Complaint
        complaint = Complaint.objects.create(
            student=student,
            course=course,
            description=description,
            priority=priority
        )
        
        # Handle Attachments
        files = request.FILES.getlist('attachments')
        for f in files:
            ComplaintAttachment.objects.create(
                complaint=complaint,
                file=f,
                file_type=f.name.split('.')[-1] if '.' in f.name else 'unknown'
            )
            
        # Register in Timeline
        UCMSService.route_complaint(complaint, user)
        
        serializer = ComplaintSerializer(complaint)
        return Response(serializer.data, status=201)
    
    # GET: List filtering based on role
    if hasattr(user, 'student'):
        complaints = Complaint.objects.filter(student=user.student)
    elif hasattr(user, 'lecturer'):
        # Show complaints for courses this lecturer teaches
        complaints = Complaint.objects.filter(course__lecturer=user.lecturer).distinct()
    else: # Admin (is_staff or is_superuser)
        complaints = Complaint.objects.all()
        
    serializer = ComplaintSerializer(complaints.order_by('-created_at'), many=True)
    return Response({"items": serializer.data})

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def support_ticket_detail(request, ticket_id):
    try:
        complaint = Complaint.objects.get(id=ticket_id)
        
        if request.method == "POST":
            message_text = request.data.get("message")
            if not message_text:
                return Response({"detail": "Message is required"}, status=400)
            from student_api.support.models import ComplaintComment
            comment = ComplaintComment.objects.create(
                complaint=complaint,
                user=request.user,
                message=message_text
            )
            return Response({
                "id": str(comment.id),
                "sender_name": f"{request.user.first_name} {request.user.last_name}",
                "message": comment.message,
                "created_at": comment.created_at.isoformat(),
                "is_staff": request.user.is_staff
            }, status=201)
            
        serializer = ComplaintSerializer(complaint)
        return Response(serializer.data)
    except Complaint.DoesNotExist:
        return Response({"detail": "Not found"}, status=404)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def update_ticket_status(request, ticket_id):
    try:
        complaint = Complaint.objects.get(id=ticket_id)
        new_status = request.data.get('status')
        if not new_status:
            return Response({"detail": "status is required"}, status=400)
            
        old_status = complaint.status
        complaint.status = new_status
        complaint.save()
        
        UCMSService.log_event(
            complaint=complaint,
            event_type='status_change',
            description=f"Status changed from {old_status} to {new_status}.",
            user=request.user
        )
        
        return Response({"status": "Updated"})
    except Complaint.DoesNotExist:
        return Response({"detail": "Not found"}, status=404)
