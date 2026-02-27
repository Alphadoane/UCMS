from rest_framework_simplejwt.tokens import RefreshToken
import logging
from django.contrib.auth import get_user_model
from django.contrib.auth import authenticate
from student_api.core.models import User, Role
from student_api.academics.models import Student

logger = logging.getLogger(__name__)

class AuthService:
    @staticmethod
    def authenticate_user(username: str, password: str):
        """Authenticate user against Django User model"""
        try:
            logger.info("Authenticating user: %s", username)
            
            # Use Django's built-in authentication
            # Use Django's built-in authentication
            # USERNAME_FIELD is 'email', so we pass the email as the 'username' argument
            user = authenticate(username=username, password=password)
                
            logger.debug("Authentication result: %s", user is not None)
            
            if user:
                # Use email or id, avoiding username which doesn't exist
                identifier = getattr(user, 'email', user.pk)
                logger.debug("User authenticated: %s", identifier)
                return user
            else:
                logger.warning("Authentication failed for username: %s", username)
                return None
                
        except Exception as e:
            logger.exception("Error authenticating user")
            return None
    
    @staticmethod
    def create_jwt_tokens(user):
        """Create JWT tokens for authenticated user"""
        try:
            refresh = RefreshToken.for_user(user)
            return {
                'access': str(refresh.access_token),
                'refresh': str(refresh),
            }
        except Exception as e:
            logger.error(f"Error creating JWT tokens: {e}")
            return None
    
    @staticmethod
    def get_student_profile(user):
        """Get student profile for user"""
        try:
            # Optimize: Fetch Student + Program + User in one query
            return Student.objects.select_related('program', 'user').get(user=user)
        except Student.DoesNotExist:
            return None
