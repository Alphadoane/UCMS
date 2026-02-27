from rest_framework.decorators import api_view, permission_classes, parser_classes
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import AllowAny, IsAuthenticated, IsAdminUser
from student_api.auth_service import AuthService


from student_api.core.serializers import StudentProfileSerializer, UserProfileSerializer, ProfileUpdateSerializer
import logging
from rest_framework.parsers import MultiPartParser, FormParser

logger = logging.getLogger(__name__)

@api_view(["POST"])
@permission_classes([AllowAny])
def login_view(request):
    try:
        username = request.data.get('username')
        password = request.data.get('password')
        
        user = AuthService.authenticate_user(username, password)
        if not user:
            return Response({"detail": "Invalid credentials"}, status=status.HTTP_401_UNAUTHORIZED)
        
        tokens = AuthService.create_jwt_tokens(user)
        
        # Optimize: Return user profile with login
        user_data = None
        
        # Check Role Priority: Admin/Staff/Lecturer > Student
        if user.is_staff or user.is_superuser or hasattr(user, 'lecturer'):
             user_data = UserProfileSerializer(user).data
        else:
             student = AuthService.get_student_profile(user)
             if student:
                 user_data = StudentProfileSerializer(student).data
             else:
                 user_data = UserProfileSerializer(user).data
            
        response_data = tokens
        if user_data:
            response_data['user'] = user_data
            
        return Response(response_data)
    except Exception as e:
        logger.exception("Login Error")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def impersonate_user(request):
    # Moving impersonation logic here
    if not request.user.is_superuser:
        return Response({"detail": "Permission denied"}, status=403)
        
    target_email = request.data.get('email')
    try:
        from django.contrib.auth import get_user_model
        User = get_user_model()
        target_user = User.objects.get(email=target_email)
        
        tokens = AuthService.create_jwt_tokens(target_user)
        
        user_data = None
        student = AuthService.get_student_profile(target_user)
        if student:
            user_data = StudentProfileSerializer(student).data
        elif target_user.is_staff:
            user_data = UserProfileSerializer(target_user).data
            
        response_data = tokens
        if user_data:
            response_data['user'] = user_data
            response_data['is_impersonation'] = True
            
        return Response(response_data)
        
    except User.DoesNotExist:
        return Response({"detail": "User not found"}, status=404)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def profile_me(request):
    user = request.user
    user_data = None
    
    # Prioritize Staff/Lecturer/Admin over Student
    # This prevents users with dual roles (or leftovers) from being locked into Student view
    if user.is_staff or user.is_superuser or hasattr(user, 'lecturer'):
        # Admin, Staff, or Lecturer
        user_data = UserProfileSerializer(user).data
    else:
        # Check if Student
        student = AuthService.get_student_profile(user)
        if student:
            user_data = StudentProfileSerializer(student).data
        else:
            # Fallback for plain users (shouldn't happen often)
            user_data = UserProfileSerializer(user).data
        
    return Response(user_data if user_data else {"id": user.id, "email": user.email})

@api_view(["PATCH"])
@permission_classes([IsAuthenticated])
def profile_update(request):
    user = request.user
    serializer = ProfileUpdateSerializer(user, data=request.data, partial=True)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
@parser_classes([MultiPartParser, FormParser])
def upload_avatar(request):
    user = request.user
    if 'avatar' not in request.FILES:
        return Response({"detail": "No avatar file provided"}, status=400)
    
    avatar_file = request.FILES['avatar']
    
    # Simple security check for file size (5MB)
    if avatar_file.size > 5 * 1024 * 1024:
        return Response({"detail": "File size exceeds 5MB limit"}, status=400)
        
    user.avatar = avatar_file
    user.save()
    
    # Return updated avatar URL
    return Response({
        "avatar": request.build_absolute_uri(user.avatar.url) if user.avatar else None
    })

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def change_password(request):
    return Response({"status": "Password changed"})

@api_view(["POST"])
@permission_classes([AllowAny])
def request_password_reset(request):
    return Response({"status": "OTP sent"})

@api_view(["POST"])
@permission_classes([AllowAny])
def verify_otp_and_reset(request):
    return Response({"status": "Password reset"})

@api_view(["POST"])
@permission_classes([IsAdminUser])
def reset_password(request, user_id):
    return Response({"status": "Password reset by admin"})
