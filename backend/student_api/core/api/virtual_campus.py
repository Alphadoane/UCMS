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
        except Course.DoesNotExist:
            return Response({"detail": "Course not found"}, status=404)
            
        try:
            try:
                dt_start = datetime.strptime(start_time, "%Y-%m-%d %H:%M")
            except ValueError:
                dt_start = datetime.fromisoformat(start_time.replace('Z', '+00:00'))
        except Exception as e:
            return Response({"detail": f"Invalid date format: {str(e)}"}, status=400)

        # --- REAL AGORA CHANNEL GENERATION ---
        # Channel names must be unique, URL-safe, and recognisable.
        # Pattern: <COURSE_CODE>-<unix_timestamp>  e.g. "CS101-1740930012"
        # Hyphens are safe in Agora channel names; spaces are not.
        safe_code = course_code.replace(" ", "-").upper()
        unix_ts = int(datetime.now().timestamp())
        channel_name = f"{safe_code}-{unix_ts}"

        # Internal deep-link so tapping "Join" passes the channel name to MeetingScreen
        join_url = f"ucms://meeting/{channel_name}"
        host_url = f"ucms://meeting/{channel_name}?host=true"

        room = ZoomRoom.objects.create(
            course=course,
            lecturer=lecturer,
            title=title,
            start_time=dt_start,
            join_url=join_url,
            host_url=host_url,
            meeting_id=channel_name,   # This IS the Agora channel name
            passcode=""                 # Not needed in App ID Only mode
        )
        
        return Response({
            "id": str(room.id),
            "course_code": room.course.code,
            "course_title": room.course.name,
            "start_time": room.start_time.isoformat(),
            "join_url": room.join_url,
            "host_url": room.host_url,
            "channel_name": room.meeting_id,  # Agora channel to join
            "meeting_id": room.meeting_id,
            "is_host": True
        }, status=201)

    # GET - List Rooms
    user = request.user
    rooms = []
    
    try:
        if user.is_staff or user.is_superuser:
             try:
                 lecturer = Lecturer.objects.get(user=user)
                 rooms = ZoomRoom.objects.filter(lecturer=lecturer)
             except Lecturer.DoesNotExist:
                 if user.is_superuser:
                     rooms = ZoomRoom.objects.all()
                 else:
                     rooms = []
        else:
            student = AuthService.get_student_profile(user)
            if student:
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
            "title": r.title,
            "start_time": r.start_time.isoformat(),
            "join_url": r.join_url,
            "host_url": r.host_url if (user == r.lecturer.user) else None,
            "channel_name": r.meeting_id,   # Agora channel name for App
            "meeting_id": r.meeting_id,
            "is_host": (user.id == r.lecturer.user.id)
        })
        
    return Response({"rooms": data})

@api_view(["DELETE"])
@permission_classes([IsAuthenticated])
def delete_zoom_room(request, room_id):
    try:
        room = ZoomRoom.objects.get(id=room_id)
        if room.lecturer.user != request.user and not request.user.is_superuser:
            return Response({"detail": "Not authorized to delete this room"}, status=403)
            
        room.delete()
        return Response({"detail": "Zoom Room deleted successfully"}, status=200)
    except ZoomRoom.DoesNotExist:
        return Response({"detail": "Room not found"}, status=404)
    except Exception as e:
        logger.error(f"Error deleting zoom room: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_agora_token(request):
    """
    Returns Agora connection info for the given channel.
    Project runs in App ID Only mode — no certificate required.
    The Android SDK joins with token=null which Agora accepts in this mode.
    """
    channel_name = request.query_params.get("channelName")
    uid = request.query_params.get("uid", 0)
    
    if not channel_name:
        return Response({"detail": "channelName is required"}, status=400)
    
    # App ID Only mode: return null token — Agora accepts this.
    return Response({
        "token": None,
        "appId": "993dc359746745f797dbf56740ff3b0f",
        "channelName": channel_name,
        "uid": int(uid)
    })
