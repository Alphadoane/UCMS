from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from student_api.academics.models import ZoomRoom, Course, CourseRegistration, Lecturer
from student_api.auth_service import AuthService
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def vc_zoom_rooms(request):
    if request.method == "POST":
        # Create Room (Lecturer Only)
        try:
            lecturer = Lecturer.objects.get(user=request.user)
        except Lecturer.DoesNotExist:
            return Response({"detail": "Only lecturers can create rooms"}, status=403)
            
        data = request.data
        course_code = data.get("course_code")
        title = data.get("title")
        start_time = data.get("start_time") # Expect ISO format
        
        if not all([course_code, title, start_time]):
             return Response({"detail": "Missing fields"}, status=400)
             
        try:
            course = Course.objects.get(code=course_code)
            # Optional: Check if lecturer is assigned to this course
        except Course.DoesNotExist:
            return Response({"detail": "Course not found"}, status=404)
            
        # Create logic (Mocking Zoom API for now)
        width = 400
        height = 600
        room = ZoomRoom.objects.create(
            course=course,
            lecturer=lecturer,
            title=title,
            start_time=start_time,
            join_url=f"https://zoom.us/j/mock{course.id}{int(datetime.now().timestamp())}",
            host_url=f"https://zoom.us/s/mock{course.id}{int(datetime.now().timestamp())}",
            # Use hyphens instead of spaces for Agora compatibility
            meeting_id=f"999-{course.id}-{int(datetime.now().timestamp())}",
            passcode="123456"
        )
        
        return Response({
            "id": str(room.id),
            "join_url": room.join_url,
            "host_url": room.host_url
        }, status=201)

    # GET - List Rooms
    user = request.user
    rooms = []
    
    try:
        if user.is_staff or user.is_superuser:
             # Admin sees all? Or just staff logic?
             # Let's verify if they are a lecturer
             try:
                 lecturer = Lecturer.objects.get(user=user)
                 # Lecturer sees their own rooms
                 rooms = ZoomRoom.objects.filter(lecturer=lecturer)
             except Lecturer.DoesNotExist:
                 if user.is_superuser:
                     rooms = ZoomRoom.objects.all()
                 else:
                     rooms = []
        else:
            # Student sees rooms for enrolled courses
            student = AuthService.get_student_profile(user)
            if student:
                # Registered courses
                reg_courses = CourseRegistration.objects.filter(student=student).values_list('course', flat=True)
                rooms = ZoomRoom.objects.filter(course__id__in=reg_courses)
    except Exception as e:
        logger.error(f"Error fetching zoom rooms: {e}")
        return Response({"detail": str(e)}, status=500)
        
    data = []
    for r in rooms:
        data.append({
            "id": str(r.id),
            "course_code": r.course.code,
            "course_title": r.course.name,
            "start_time": r.start_time.isoformat(),
            "join_url": r.join_url,
            "host_url": r.host_url if (user == r.lecturer.user) else None, # Only host sees host_url
            "is_host": (user.id == r.lecturer.user.id)
        })
        

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_agora_token(request):
    """
    Returns an Agora RTC token for the given channel and UID.
    In a real production environment, this would use 'agora-token-builder'.
    For local development/demo, we return a mock token or instructions.
    """
    channel_name = request.query_params.get("channelName")
    uid = request.query_params.get("uid", 0)
    
    if not channel_name:
        return Response({"detail": "channelName is required"}, status=400)
        
    # LOGIC: In App ID Only mode, we can just return a placeholder 
    # if the project is configured for testing without a certificate.
    # However, to simulate a real flow, we return a JSON that the Android app expects.
    
    # MOCK TOKEN: For testing, if no certificate is set in Agora console, 
    # joining with a null/empty token or any string might work depending on settings.
    # We provide a dummy token here.
    return Response({
        "token": f"mock_token_for_{channel_name}_{uid}",
        "appId": "993dc359746745f797dbf56740ff3b0f" # Matching the Android side
    })
