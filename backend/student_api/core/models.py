import uuid
from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.utils import timezone

# 1. Identity & Access
class UserManager(BaseUserManager):
    def create_user(self, email, password=None, **extra_fields):
        if not email:
            raise ValueError('The Email field must be set')
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        if password:
            user.set_password(password)
        else:
            user.set_unusable_password()
        user.save(using=self._db)
        return user

    def create_superuser(self, email, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        return self.create_user(email, password, **extra_fields)

class User(AbstractBaseUser, PermissionsMixin):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    email = models.EmailField(unique=True)
    first_name = models.CharField(max_length=50, blank=True)
    last_name = models.CharField(max_length=50, blank=True)
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False) 
    phone_number = models.CharField(max_length=20, blank=True, null=True)
    alternate_email = models.EmailField(blank=True, null=True)
    address = models.TextField(blank=True, null=True)
    bio = models.TextField(blank=True, null=True)
    avatar = models.ImageField(upload_to='avatars/', blank=True, null=True)
    created_at = models.DateTimeField(default=timezone.now)

    objects = UserManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

    class Meta:
        db_table = 'users'

class Role(models.Model):
    name = models.CharField(max_length=50, unique=True)
    
    class Meta:
        db_table = 'roles'

class UserRole(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    role = models.ForeignKey(Role, on_delete=models.CASCADE)
    
    class Meta:
        db_table = 'user_roles'
        unique_together = ('user', 'role')

class SystemSettings(models.Model):
    student_email_domain = models.CharField(max_length=100, default='students.kcau.ac.ke')
    last_student_seq = models.IntegerField(default=0)
    student_id_prefix = models.CharField(max_length=10, default='30')
    
    class Meta:
        db_table = 'system_settings'
        verbose_name_plural = 'System Settings'
        
    @classmethod
    def get_domain(cls):
        obj, created = cls.objects.get_or_create(id=1)
        return obj.student_email_domain

# 9. Communication (Can stay in Core for now or move to 'communication' app later)
class Broadcast(models.Model):
    TARGET_CHOICES = [
        ('ALL', 'All Users'),
        ('STUDENT', 'Students Only'),
        ('STAFF', 'Staff Only'),
    ]
    
    title = models.CharField(max_length=200)
    message = models.TextField()
    target_audience = models.CharField(max_length=20, choices=TARGET_CHOICES, default='ALL')
    sender = models.ForeignKey(User, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'broadcasts'
        ordering = ['-created_at']

class PasswordResetOTP(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    otp_code = models.CharField(max_length=6)
    created_at = models.DateTimeField(auto_now_add=True)
    is_used = models.BooleanField(default=False)
    
    class Meta:
        db_table = 'password_reset_otps'
    
    def is_valid(self):
        # Valid for 15 minutes
        from django.utils import timezone
        return not self.is_used and (timezone.now() - self.created_at).total_seconds() < 900
