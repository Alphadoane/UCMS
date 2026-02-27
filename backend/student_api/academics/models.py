from django.db import models
from django.conf import settings
# Use settings.AUTH_USER_MODEL for User references to avoid circular imports or hard deps
from student_api.core.models import User # Or use settings.AUTH_USER_MODEL

# 2. Organization
class Faculty(models.Model):
    name = models.CharField(max_length=100, unique=True)
    
    class Meta:
        db_table = 'faculties'

class Department(models.Model):
    faculty = models.ForeignKey(Faculty, on_delete=models.CASCADE)
    name = models.CharField(max_length=100)
    
    class Meta:
        db_table = 'departments'
        unique_together = ('faculty', 'name')

# 3. Academic
class Program(models.Model):
    department = models.ForeignKey(Department, on_delete=models.CASCADE)
    code = models.CharField(max_length=20, unique=True)
    name = models.CharField(max_length=100)
    duration_years = models.IntegerField()
    category = models.CharField(max_length=50, default="Undergraduate") # e.g. Doctoral, Masters, Undergraduate
    entry_requirements = models.TextField(blank=True)
    
    class Meta:
        db_table = 'programs'

class Course(models.Model):
    program = models.ForeignKey(Program, on_delete=models.CASCADE)
    code = models.CharField(max_length=20)
    name = models.CharField(max_length=100)
    credit_units = models.IntegerField()
    
    class Meta:
        db_table = 'courses'
        unique_together = ('program', 'code')

class AcademicYear(models.Model):
    year_label = models.CharField(max_length=20, unique=True) # e.g., "2023/2024"
    
    class Meta:
        db_table = 'academic_years'

class Semester(models.Model):
    academic_year = models.ForeignKey(AcademicYear, on_delete=models.CASCADE)
    name = models.CharField(max_length=50) # e.g., "Semester 1"
    start_date = models.DateField()
    end_date = models.DateField()
    
    class Meta:
        db_table = 'semesters'
        unique_together = ('academic_year', 'name')

# 4. Profiles (Moved from Core)
class Student(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, primary_key=True)
    admission_number = models.CharField(max_length=50, unique=True)
    program = models.ForeignKey(Program, on_delete=models.CASCADE)
    admission_date = models.DateField()
    STATUS_CHOICES = [
        ('active', 'Active'),
        ('suspended', 'Suspended'),
        ('graduated', 'Graduated'),
        ('withdrawn', 'Withdrawn'),
    ]
    status = models.CharField(max_length=20, choices=STATUS_CHOICES)
    mpesa_phone = models.CharField(max_length=15, blank=True, null=True)
    
    class Meta:
        db_table = 'students'

class Lecturer(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, primary_key=True)
    department = models.ForeignKey(Department, on_delete=models.CASCADE)
    employee_id = models.CharField(max_length=50, unique=True, null=True, blank=True)
    employment_type = models.CharField(max_length=50)
    
    class Meta:
        db_table = 'lecturers'

# 5. Course Registration & Assessment
class CourseRegistration(models.Model):
    student = models.ForeignKey(Student, on_delete=models.CASCADE)
    course = models.ForeignKey(Course, on_delete=models.CASCADE)
    semester = models.ForeignKey(Semester, on_delete=models.CASCADE)
    registered_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'course_registrations'
        unique_together = ('student', 'course', 'semester')

class Grade(models.Model):
    registration = models.OneToOneField(CourseRegistration, on_delete=models.CASCADE)
    score = models.DecimalField(max_digits=5, decimal_places=2)
    grade = models.CharField(max_length=5)
    approved = models.BooleanField(default=False)
    approved_at = models.DateTimeField(null=True, blank=True)
    
    class Meta:
        db_table = 'grades'

class CourseWork(models.Model):
    course = models.ForeignKey(Course, on_delete=models.CASCADE)
    lecturer = models.ForeignKey(Lecturer, on_delete=models.CASCADE)
    title = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    max_marks = models.DecimalField(max_digits=5, decimal_places=2)
    due_date = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'course_work'

class Submission(models.Model):
    course_work = models.ForeignKey(CourseWork, related_name='submissions', on_delete=models.CASCADE)
    student = models.ForeignKey(Student, on_delete=models.CASCADE)
    file = models.FileField(upload_to='submissions/%Y/%m/', blank=True, null=True)
    submitted_at = models.DateTimeField(auto_now_add=True, db_index=True)
    score = models.DecimalField(max_digits=5, decimal_places=2, null=True, blank=True)
    remarks = models.TextField(blank=True)

    class Meta:
        db_table = 'submissions'
        unique_together = ('course_work', 'student')

# Exams
class ExamSession(models.Model):
    course = models.ForeignKey(Course, on_delete=models.CASCADE)
    semester = models.ForeignKey(Semester, on_delete=models.CASCADE)
    date = models.DateField()
    start_time = models.TimeField()
    end_time = models.TimeField()
    venue = models.CharField(max_length=100)
    invigilator = models.CharField(max_length=100, blank=True)
    
    class Meta:
        db_table = 'exam_sessions'

class Lecture(models.Model):
    course = models.ForeignKey(Course, on_delete=models.CASCADE)
    lecturer = models.ForeignKey(Lecturer, on_delete=models.CASCADE)
    day_of_week = models.CharField(max_length=10) # Monday, Tuesday...
    start_time = models.TimeField()
    end_time = models.TimeField()
    venue = models.CharField(max_length=50)
    
    class Meta:
        db_table = 'lectures'

class ZoomRoom(models.Model):
    course = models.ForeignKey(Course, on_delete=models.CASCADE)
    lecturer = models.ForeignKey(Lecturer, on_delete=models.CASCADE)
    title = models.CharField(max_length=200)
    start_time = models.DateTimeField()
    duration_minutes = models.IntegerField(default=60)
    join_url = models.URLField(max_length=500)
    host_url = models.URLField(max_length=500, blank=True, null=True)
    meeting_id = models.CharField(max_length=50, blank=True)
    passcode = models.CharField(max_length=50, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'zoom_rooms'
