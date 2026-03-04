from rest_framework import serializers
from student_api.core.models import User, Role
from student_api.academics.models import (
    Student, Program, Course, CourseRegistration, Grade, 
    CourseWork, Submission, ExamSession, Lecture, Lecturer, 
    LearningMaterial
)
from student_api.finance.models import Payment
from student_api.support.models import Complaint, ComplaintAttachment, ComplaintComment, ComplaintTimeline

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'email', 'first_name', 'last_name', 'phone_number', 'alternate_email', 'address', 'bio', 'avatar']

class StudentProfileSerializer(serializers.ModelSerializer):
    id = serializers.CharField(source='user.id')
    full_name = serializers.SerializerMethodField()
    email = serializers.EmailField(source='user.email')
    course = serializers.CharField(source='program.name')
    admission_no = serializers.CharField(source='admission_number')
    role = serializers.SerializerMethodField()
    phone_number = serializers.CharField(source='user.phone_number', read_only=True)
    alternate_email = serializers.EmailField(source='user.alternate_email', read_only=True)
    address = serializers.CharField(source='user.address', read_only=True)
    bio = serializers.CharField(source='user.bio', read_only=True)
    avatar = serializers.ImageField(source='user.avatar', read_only=True)
    
    class Meta:
        model = Student
        fields = [
            'id', 'admission_no', 'full_name', 'email', 'course', 'role',
            'phone_number', 'alternate_email', 'address', 'bio', 'avatar'
        ]
        
    def get_full_name(self, obj):
        return f"{obj.user.first_name} {obj.user.last_name}"

    def get_role(self, obj):
        return "STUDENT"

class UserProfileSerializer(serializers.ModelSerializer):
    id = serializers.CharField()
    full_name = serializers.SerializerMethodField()
    email = serializers.EmailField()
    role = serializers.SerializerMethodField()
    # Admin/Staff don't have admission_no or course, send nulls for compatibility if needed, 
    # or let the frontend handle nulls (which we will ensure).
    admission_no = serializers.SerializerMethodField()
    course = serializers.SerializerMethodField()

    avatar = serializers.ImageField(read_only=True)

    class Meta:
        model = User
        fields = [
            'id', 'admission_no', 'full_name', 'email', 'course', 'role',
            'phone_number', 'alternate_email', 'address', 'bio', 'avatar'
        ]

    def get_full_name(self, obj):
        return f"{obj.first_name} {obj.last_name}"

    def get_role(self, obj):
        if obj.is_superuser: return "ADMIN"
        if hasattr(obj, 'lecturer'): return "LECTURER"
        if obj.is_staff: return "STAFF"
        return "USER"

    def get_admission_no(self, obj):
        if obj.is_staff and not obj.is_superuser:
            try:
                return obj.lecturer.employee_id
            except:
                return None
        return None

    def get_course(self, obj): 
        if obj.is_staff and not obj.is_superuser:
            try:
                return obj.lecturer.department.name
            except:
                return None
        return None

class ProfileUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['phone_number', 'alternate_email', 'address', 'bio']

class CourseRegistrationSerializer(serializers.ModelSerializer):
    course_code = serializers.CharField(source='course.code')
    course_name = serializers.CharField(source='course.name')
    credits = serializers.IntegerField(source='course.credit_units')
    semester = serializers.CharField(source='semester.name')
    status = serializers.SerializerMethodField()
    
    class Meta:
        model = CourseRegistration
        fields = ['course_code', 'course_name', 'credits', 'semester', 'status']
    
    def get_status(self, obj):
        return "Registered"

class StudentGradeSerializer(serializers.ModelSerializer):
    course_code = serializers.CharField(source='registration.course.code')
    assignment = serializers.SerializerMethodField()
    marks = serializers.FloatField(source='score')
    max_marks = serializers.SerializerMethodField()
    due_date = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    
    class Meta:
        model = Grade
        fields = ['course_code', 'assignment', 'marks', 'max_marks', 'due_date', 'status']

    def get_assignment(self, obj):
        return "Final Exam" 

    def get_max_marks(self, obj):
        return 100
        
    def get_due_date(self, obj):
        return "2023-12-15"
        
    def get_status(self, obj):
        return "Graded" if obj.approved else "Pending"

class CourseWorkSerializer(serializers.ModelSerializer):
    lecturer_name = serializers.CharField(source='lecturer.user.first_name', read_only=True)
    course_name = serializers.CharField(source='course.name', read_only=True)
    
    class Meta:
        model = CourseWork
        fields = ['id', 'course', 'course_name', 'lecturer', 'lecturer_name', 'category', 'title', 'description', 'max_marks', 'due_date', 'created_at']
        read_only_fields = ['course', 'lecturer']

class LearningMaterialSerializer(serializers.ModelSerializer):
    lecturer_name = serializers.CharField(source='lecturer.user.first_name', read_only=True)
    course_name = serializers.CharField(source='course.name', read_only=True)
    
    class Meta:
        model = LearningMaterial
        fields = ['id', 'course', 'course_name', 'lecturer', 'lecturer_name', 'title', 'description', 'file', 'link', 'created_at']
        read_only_fields = ['course', 'lecturer']

class SubmissionSerializer(serializers.ModelSerializer):
    student_name = serializers.CharField(source='student.user.first_name', read_only=True)
    admission_no = serializers.CharField(source='student.admission_number', read_only=True)
    
    class Meta:
        model = Submission
        fields = ['id', 'course_work', 'student', 'student_name', 'admission_no', 'file', 'submitted_at', 'score', 'remarks']
        fields = ['id', 'course_work', 'student', 'student_name', 'admission_no', 'file', 'submitted_at', 'score', 'remarks']
        read_only_fields = ['submitted_at', 'score', 'remarks', 'student']

class CourseOptionSerializer(serializers.ModelSerializer):
    class Meta:
        model = Course
        fields = ['id', 'code', 'name']

class LecturerOptionSerializer(serializers.ModelSerializer):
    name = serializers.SerializerMethodField()
    class Meta:
        model = Lecturer
        fields = ['user_id', 'employee_id', 'name']
    
    def get_name(self, obj):
        return f"{obj.user.first_name} {obj.user.last_name}"

class StudentOptionSerializer(serializers.ModelSerializer):
    name = serializers.SerializerMethodField()
    class Meta:
        model = Student
        fields = ['admission_number', 'name']
    
    def get_name(self, obj):
        return f"{obj.user.first_name} {obj.user.last_name}"

class ExamSessionSerializer(serializers.ModelSerializer):
    course_code = serializers.CharField(source='course.code')
    course_name = serializers.CharField(source='course.name')
    
    class Meta:
        model = ExamSession
        fields = ['id', 'course_code', 'course_name', 'date', 'start_time', 'end_time', 'venue']

class ExamResultSerializer(serializers.ModelSerializer):
    course_code = serializers.CharField(source='registration.course.code')
    course_name = serializers.CharField(source='registration.course.name')
    grade_letter = serializers.CharField(source='grade')
    
    class Meta:
        model = Grade
        fields = ['course_code', 'course_name', 'score', 'grade_letter', 'approved']

class ComplaintAttachmentSerializer(serializers.ModelSerializer):
    class Meta:
        model = ComplaintAttachment
        fields = ['id', 'file', 'file_type', 'uploaded_at']

class ComplaintCommentSerializer(serializers.ModelSerializer):
    user_name = serializers.SerializerMethodField()
    
    class Meta:
        model = ComplaintComment
        fields = ['id', 'user_name', 'message', 'created_at']
        
    def get_user_name(self, obj):
        return f"{obj.user.first_name} {obj.user.last_name}"

class ComplaintTimelineSerializer(serializers.ModelSerializer):
    user_name = serializers.SerializerMethodField()
    
    class Meta:
        model = ComplaintTimeline
        fields = ['id', 'event_type', 'description', 'user_name', 'created_at']
        
    def get_user_name(self, obj):
        if obj.user:
            return f"{obj.user.first_name} {obj.user.last_name}"
        return "System"

class ComplaintSerializer(serializers.ModelSerializer):
    attachments = ComplaintAttachmentSerializer(many=True, read_only=True)
    comments = ComplaintCommentSerializer(many=True, read_only=True)
    timeline = ComplaintTimelineSerializer(many=True, read_only=True)
    course_name = serializers.CharField(source='course.name', read_only=True)
    student_name = serializers.SerializerMethodField()
    
    title = serializers.CharField(source='description', read_only=True)
    category = serializers.SerializerMethodField()

    class Meta:
        model = Complaint
        fields = [
            'id', 'student', 'student_name', 'course', 'course_name', 
            'description', 'title', 'category', 'status', 'priority', 'created_at', 
            'attachments', 'comments', 'timeline'
        ]
        read_only_fields = ['id', 'status', 'created_at']

    def get_category(self, obj):
        return "Academic" # Default category as expected by frontend

    def get_student_name(self, obj):
        return f"{obj.student.user.first_name} {obj.student.user.last_name}"

class PaymentSerializer(serializers.ModelSerializer):
    receipt_no = serializers.CharField(source='reference')
    date = serializers.DateTimeField(source='paid_at')
    description = serializers.CharField(source='payment_method')
    
    class Meta:
        model = Payment
        fields = ['receipt_no', 'amount', 'date', 'description']

class LectureSerializer(serializers.ModelSerializer):
    course_code = serializers.CharField(source='course.code', read_only=True)
    course_title = serializers.CharField(source='course.name', read_only=True)
    day = serializers.CharField(source='day_of_week')
    
    class Meta:
        model = Lecture
        fields = ['id', 'course_code', 'course_title', 'day', 'start_time', 'end_time', 'venue']
