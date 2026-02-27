from django.db import models
from django.conf import settings
from student_api.academics.models import Student, Course

class Complaint(models.Model):
    STATUS_CHOICES = [
        ('submitted', 'Submitted'),
        ('under_review', 'Under Review'),
        ('lecturer_responded', 'Lecturer Responded'),
        ('admin_processing', 'Admin Processing'),
        ('resolved', 'Resolved'),
        ('closed', 'Closed'),
        ('escalated', 'Escalated'),
    ]
    PRIORITY_CHOICES = [
        ('low', 'Low'),
        ('medium', 'Medium'),
        ('high', 'High'),
        ('urgent', 'Urgent'),
    ]
    
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='complaints')
    course = models.ForeignKey(Course, on_delete=models.CASCADE, related_name='complaints')
    description = models.TextField()
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='submitted', db_index=True)
    priority = models.CharField(max_length=20, choices=PRIORITY_CHOICES, default='medium')
    created_at = models.DateTimeField(auto_now_add=True, db_index=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'complaints'

class ComplaintAttachment(models.Model):
    complaint = models.ForeignKey(Complaint, related_name='attachments', on_delete=models.CASCADE)
    file = models.FileField(upload_to='complaints/%Y/%m/')
    file_type = models.CharField(max_length=50) # e.g. pdf, docx, image
    uploaded_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'complaint_attachments'

class ComplaintComment(models.Model):
    complaint = models.ForeignKey(Complaint, related_name='comments', on_delete=models.CASCADE)
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'complaint_comments'

class ComplaintTimeline(models.Model):
    complaint = models.ForeignKey(Complaint, related_name='timeline', on_delete=models.CASCADE)
    event_type = models.CharField(max_length=50) # e.g. status_change, comment_added
    description = models.TextField()
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'complaint_timeline'

class CampusLifeContent(models.Model):
    title = models.CharField(max_length=200)
    image = models.ImageField(upload_to='campus_life/%Y/%m/')
    description = models.TextField()
    category = models.CharField(max_length=50, default='General')
    created_at = models.DateTimeField(auto_now_add=True)
    author = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL, null=True)

    class Meta:
        db_table = 'campus_life_content'
        ordering = ['-created_at']

class Appointment(models.Model):
    TYPE_CHOICES = [
        ('HEALTH', 'General Health'),
        ('MENTAL_HEALTH', 'Mental Health/Counseling'),
        ('THERAPY', 'Therapy Session'),
    ]
    STATUS_CHOICES = [
        ('PENDING', 'Pending'),
        ('CONFIRMED', 'Confirmed'),
        ('CANCELLED', 'Cancelled'),
        ('COMPLETED', 'Completed'),
    ]
    
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='appointments')
    appointment_type = models.CharField(max_length=20, choices=TYPE_CHOICES, default='HEALTH')
    reason = models.TextField()
    appointment_date = models.DateTimeField()
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')
    admin_notes = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'appointments'
        ordering = ['-appointment_date']

class EmergencyAlert(models.Model):
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='emergency_alerts')
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    message = models.TextField(default="Emergency Distress Signal")
    status = models.CharField(max_length=20, default='ACTIVE') # ACTIVE, RESOLVED
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'emergency_alerts'
        ordering = ['-created_at']
