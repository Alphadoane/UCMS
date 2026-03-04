from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated, IsAdminUser
from student_api.auth_service import AuthService
# Updated Imports
from student_api.academics.models import CourseRegistration, Grade, ExamSession, Lecture, Course, Lecturer, Student, Semester, CourseWork
from student_api.core.serializers import *
import logging

logger = logging.getLogger(__name__)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_course_registration(request):
    student = AuthService.get_student_profile(request.user)
    if not student: return Response({"detail": "No profile"}, status=404)
    
    regs = CourseRegistration.objects.filter(student=student).select_related('course', 'semester')
    serializer = CourseRegistrationSerializer(regs, many=True)
    return Response({"items": serializer.data})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_exam_card(request):
    student = AuthService.get_student_profile(request.user)
    if not student: return Response({"detail": "No profile"}, status=404)
    
    # Real Exam Card Logic
    # 1. Check if fees cleared (Mocking finance check for now, ensuring balance <= 0)
    # finance_status = student.finance_status (Future)
    
    registered_courses = CourseRegistration.objects.filter(student=student).values_list('course_id', flat=True)
    exams = ExamSession.objects.filter(
        course_id__in=registered_courses
    ).select_related('course').distinct()
    
    serializer = ExamSessionSerializer(exams, many=True)
    return Response({"status": "Card Available", "exams": serializer.data}) # Use Serializer

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_exam_result(request):
    student = AuthService.get_student_profile(request.user)
    if not student: return Response({"detail": "No profile"}, status=404)
    
    results = Grade.objects.filter(registration__student=student).select_related('registration__course')
    serializer = ExamResultSerializer(results, many=True)
    return Response({"results": serializer.data}) # Use Serializer

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_timetable(request):
    user = request.user
    try:
        from student_api.academics.models import Lecturer
        lecturer = Lecturer.objects.get(user=user)
        lectures = Lecture.objects.filter(lecturer=lecturer).select_related('course')
    except: # Generic except to catch import error or DoesNotExist
         student = AuthService.get_student_profile(user)
         if student:
             registered_courses = CourseRegistration.objects.filter(student=student).values_list('course_id', flat=True)
             lectures = Lecture.objects.filter(
                 course_id__in=registered_courses
             ).select_related('course').distinct()
         else:
             return Response([])
         
    serializer = LectureSerializer(lectures, many=True)
    return Response(serializer.data)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_course_work(request):
    student = AuthService.get_student_profile(request.user)
    if not student: return Response({"detail": "No profile"}, status=404)

    # Get coursework for registered courses
    # Get coursework for registered courses
    # Fix: Use explicit subquery or ID list to avoid relation traversal issues
    registered_course_ids = CourseRegistration.objects.filter(student=student).values_list('course_id', flat=True)
    assignments = CourseWork.objects.filter(
        course_id__in=registered_course_ids
    ).select_related('course', 'lecturer__user').order_by('-created_at')
    
    serializer = CourseWorkSerializer(assignments, many=True)
    return Response({"items": serializer.data})

@api_view(["POST"])
@permission_classes([IsAdminUser])
def admin_allocate_lecture(request):
    data = request.data
    print(f"DEBUG: Allocation request keys: {list(data.keys())}")
    print(f"DEBUG: course_id value: {data.get('course_id')}")
    
    course_id = data.get('course_id')
    if course_id is None:
        return Response({"detail": f"Missing course_id. Data received: {data}"}, status=400)
    
    employee_id = data.get('employee_id')
    if employee_id is None:
        return Response({"detail": "Missing employee_id"}, status=400)
    
    try:
        from datetime import datetime
        course = Course.objects.get(id=course_id)
        lecturer = Lecturer.objects.get(employee_id=employee_id)
        
        # Parse times robustly
        def parse_time(t_str):
            if not t_str: return None
            for fmt in ("%H:%M", "%I:%M %p", "%I:%M%p", "%H:%M:%S"):
                try:
                    return datetime.strptime(t_str, fmt).time()
                except ValueError:
                    continue
            # Fallback for simple H:M
            try:
                h, m = map(int, t_str.split(':'))
                from datetime import time
                return time(h, m)
            except:
                raise ValueError(f"Illegal time format: {t_str}")

        s_time_str = data.get('start_time')
        e_time_str = data.get('end_time')
        print(f"DEBUG: Parsing times: {s_time_str} to {e_time_str}")
        
        s_time = parse_time(s_time_str)
        e_time = parse_time(e_time_str)
        
        # Create Lecture
        lecture = Lecture.objects.create(
            course=course,
            lecturer=lecturer,
            day_of_week=data.get('day'),
            start_time=s_time,
            end_time=e_time,
            venue=data.get('venue', '')
        )
        print(f"DEBUG: Lecture created: {lecture.id}")
        
        # Handle Student Allocation
        student_ids = data.get('student_ids', [])
        if student_ids:
            print(f"DEBUG: Allocating students: {student_ids}")
            # Simple logic for now: Latest semester
            semester = Semester.objects.order_by('-end_date').first()
            if semester:
                for admin_no in student_ids:
                    try:
                        student = Student.objects.get(admission_number=admin_no)
                        CourseRegistration.objects.get_or_create(
                            student=student,
                            course=course,
                            semester=semester
                        )
                    except Student.DoesNotExist:
                        print(f"DEBUG: Student {admin_no} not found")
                        continue
            else:
                logger.warning("No semester found for allocation")
                
        serializer = LectureSerializer(lecture)
        return Response(serializer.data, status=201)
        
    except Course.DoesNotExist:
        return Response({"detail": f"Course with ID {course_id} not found"}, status=404)
    except Lecturer.DoesNotExist:
        return Response({"detail": f"Lecturer {employee_id} not found"}, status=404)
    except Exception as e:
        print(f"DEBUG: Allocation Error: {str(e)}")
        logger.error(f"Error in allocation: {e}")
        return Response({"detail": f"Allocation failed: {str(e)}"}, status=400)

@api_view(["GET"])
@permission_classes([IsAdminUser])
def admin_allocation_options(request):
    # Provide data for dropdowns
    courses = CourseOptionSerializer(Course.objects.all(), many=True).data
    lecturers = LecturerOptionSerializer(Lecturer.objects.all(), many=True).data
    # Rooms hardcoded for now or model
    rooms = [{"id": "LAB1", "name": "Computer Lab 1"}, {"id": "LT1", "name": "Lecture Theatre 1"}]
    students = StudentOptionSerializer(Student.objects.filter(status='active'), many=True).data
    return Response({"courses": courses, "lecturers": lecturers, "rooms": rooms, "students": students})

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def lecturer_course_work(request):
    try:
        from student_api.academics.models import Lecturer
        lecturer = Lecturer.objects.get(user=request.user)
    except Exception:
        return Response({"detail": "Access restricted"}, status=403)
        
    if request.method == "POST":
        serializer = CourseWorkSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(lecturer=lecturer) # Force lecturer
            return Response(serializer.data, status=201)
        return Response(serializer.errors, status=400)
    
    # GET
    works = CourseWork.objects.filter(lecturer=lecturer).order_by('-created_at')
    return Response({"assignments": CourseWorkSerializer(works, many=True).data})

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def student_submit_work(request, work_id):
    student = AuthService.get_student_profile(request.user)
    if not student: return Response({"detail": "Access restricted to students"}, status=403)
    
    return Response({"status": "Submitted"})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_exam_audit(request):
    return Response({"audit": []})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_academic_leave(request):
    return Response({"leaves": []})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def academics_clearance(request):
    return Response({"clearance": {}})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def staff_courses(request):
    try:
        from student_api.academics.models import Lecturer, Lecture, Course, Program
        # Check if user is lecturer
        try:
            lecturer = Lecturer.objects.get(user=request.user)
        except Lecturer.DoesNotExist:
            return Response({"detail": "Not a lecturer"}, status=403)

        # Option 1: Courses they teach via Lecture schedule
        lecture_courses = Lecture.objects.filter(lecturer=lecturer).values_list('course', flat=True).distinct()
        
        # Option 2: Fetch courses
        courses = Course.objects.filter(id__in=lecture_courses)
        
        # Fallback: All courses in their department if no specific lectures assigned
        if not courses.exists() and lecturer.department:
             # Find programs in dept
             programs = Program.objects.filter(department=lecturer.department)
             courses = Course.objects.filter(program__in=programs)[:20] # Limit for safety
        
        data = []
        for c in courses:
            # Count students
            student_count = CourseRegistration.objects.filter(course=c).count()
            data.append({
                "id": c.id,
                "code": c.code,
                "title": c.name,
                "student_count": student_count,
                "semester": "Current" # Placeholder
            })
            
        return Response(data)
        
    except Exception as e:
        logger.error(f"Error fetching staff courses: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def staff_course_students(request, course_id):
    """
    Returns a list of students enrolled in a specific course.
    """
    try:
        lecturer = AuthService.get_lecturer_profile(request.user)
        if not lecturer:
            return Response({"detail": "Not a lecturer"}, status=403)

        # Check if course exists
        try:
            course = Course.objects.get(id=course_id)
        except Course.DoesNotExist:
            return Response({"detail": "Course not found"}, status=404)

        # Fetch students registered for this course
        # Use select_related for efficiency
        registrations = CourseRegistration.objects.filter(
            course_id=course_id
        ).select_related('student__user', 'student__program', 'semester')

        data = []
        for reg in registrations:
            student = reg.student
            data.append({
                "admission_number": student.admission_number,
                "full_name": f"{student.user.first_name} {student.user.last_name}",
                "program": student.program.name,
                "semester": reg.semester.name,
                "registered_at": reg.registered_at
            })

        return Response(data)

    except Exception as e:
        logger.error(f"Error fetching course students: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def staff_course_work(request, course_id):
    """
    GET: List assignments and CATs for a specific course.
    POST: Create a new assignment or CAT for a specific course.
    """
    try:
        lecturer = AuthService.get_lecturer_profile(request.user)
        if not lecturer:
            return Response({"detail": "Not a lecturer"}, status=403)

        try:
            course = Course.objects.get(id=course_id)
        except Course.DoesNotExist:
            return Response({"detail": "Course not found"}, status=404)

        if request.method == "POST":
            serializer = CourseWorkSerializer(data=request.data)
            if serializer.is_valid():
                serializer.save(lecturer=lecturer, course=course)
                return Response(serializer.data, status=201)
            return Response(serializer.errors, status=400)

        # GET
        category = request.query_params.get('category')
        works = CourseWork.objects.filter(course=course, lecturer=lecturer)
        if category:
            works = works.filter(category=category)
        
        serializer = CourseWorkSerializer(works.order_by('-created_at'), many=True)
        return Response(serializer.data)

    except Exception as e:
        logger.error(f"Error in staff_course_work: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def staff_course_materials(request, course_id):
    """
    GET: List learning materials for a specific course.
    POST: Create a new learning material for a specific course.
    """
    try:
        lecturer = AuthService.get_lecturer_profile(request.user)
        if not lecturer:
            return Response({"detail": "Not a lecturer"}, status=403)

        try:
            course = Course.objects.get(id=course_id)
        except Course.DoesNotExist:
            return Response({"detail": "Course not found"}, status=404)

        if request.method == "POST":
            serializer = LearningMaterialSerializer(data=request.data)
            if serializer.is_valid():
                serializer.save(lecturer=lecturer, course=course)
                return Response(serializer.data, status=201)
            return Response(serializer.errors, status=400)

        # GET
        materials = LearningMaterial.objects.filter(course=course, lecturer=lecturer).order_by('-created_at')
        serializer = LearningMaterialSerializer(materials, many=True)
        return Response(serializer.data)

    except Exception as e:
        logger.error(f"Error in staff_course_materials: {e}")
        return Response({"detail": str(e)}, status=500)

from student_api.academics.utils import generate_pdf_slip
from django.http import HttpResponse

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_result_slip(request):
    try:
        student = AuthService.get_student_profile(request.user)
        if not student: return Response({"detail": "Student profile not found"}, status=404)

        # Get latest semester or key parameter
        semester_id = request.query_params.get('semester_id')
        if semester_id:
            try:
                semester = Semester.objects.get(id=semester_id)
            except Semester.DoesNotExist:
                return Response({"detail": "Semester not found"}, status=404)
        else:
            semester = Semester.objects.order_by('-end_date').first()
            if not semester: return Response({"detail": "No active semester found"}, status=404)

        # Fetch results
        results = Grade.objects.filter(
            registration__student=student,
            registration__semester=semester,
            approved=True 
        ).select_related('registration__course')

        data = []
        total_points = 0.0
        total_credits = 0.0
        
        for r in results:
            course = r.registration.course
            grade = r.grade
            score = float(r.score)
            points = 0.0
            if grade == 'A': points = 4.0
            elif grade == 'B': points = 3.0
            elif grade == 'C': points = 2.0
            elif grade == 'D': points = 1.0
            
            total_points += points * course.credit_units
            total_credits += course.credit_units
            
            data.append({
                "course_code": course.code,
                "course_title": course.name,
                "credits": course.credit_units,
                "grade": grade,
                "score": score,
                "points": points * course.credit_units
            })
            
        gpa = total_points / total_credits if total_credits > 0 else 0.0
        
        return Response({
            "student_name": f"{student.user.first_name} {student.user.last_name}",
            "admission_number": student.admission_number,
            "program": student.program.name,
            "semester": semester.name,
            "semester_id": semester.id,
            "academic_year": semester.academic_year.year_label,
            "results": data,
            "total_credits": total_credits,
            "gpa": round(gpa, 2)
        })

    except Exception as e:
        logger.error(f"Error generating result slip: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def download_result_slip(request):
    try:
        student = AuthService.get_student_profile(request.user)
        if not student: return Response({"detail": "Student profile not found"}, status=404)

        semester_id = request.query_params.get('semester_id')
        if semester_id:
            try:
                semester = Semester.objects.get(id=semester_id)
            except Semester.DoesNotExist:
                return Response({"detail": "Semester not found"}, status=404)
        else:
            semester = Semester.objects.order_by('-end_date').first()
        
        if not semester: return Response({"detail": "No semester data"}, status=404)
        
        results = Grade.objects.filter(
            registration__student=student,
            registration__semester=semester,
            approved=True
        ).select_related('registration__course')
        
        res_list = []
        total_points = 0.0
        total_credits = 0.0
        
        for r in results:
            c = r.registration.course
            grade = r.grade
            points = 0.0
            if grade == 'A': points = 4.0
            elif grade == 'B': points = 3.0
            elif grade == 'C': points = 2.0
            elif grade == 'D': points = 1.0
            
            total_points += points * c.credit_units
            total_credits += c.credit_units
            
            res_list.append({
                "course_code": c.code,
                "course_title": c.name,
                "credits": c.credit_units,
                "grade": grade
            })
            
        gpa = total_points / total_credits if total_credits > 0 else 0.0
        
        pdf_buffer = generate_pdf_slip(student, semester.name, res_list, gpa, total_credits)
        
        response = HttpResponse(pdf_buffer, content_type='application/pdf')
        filename = f"ResultSlip_{student.admission_number}_{semester.name.replace(' ', '_')}.pdf"
        response['Content-Disposition'] = f'attachment; filename="{filename}"'
        return response

    except Exception as e:
        logger.error(f"Error downloading PDF: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([IsAdminUser])
def publish_results(request):
    try:
        semester_id = request.data.get('semester_id')
        if not semester_id:
            return Response({"detail": "semester_id required"}, status=400)
            
        course_id = request.data.get('course_id')
        
        grades = Grade.objects.filter(registration__semester_id=semester_id)
        if course_id:
            grades = grades.filter(registration__course__id=course_id)
            
        count = grades.update(approved=True)
        return Response({"detail": f"Published {count} grades successfully"})
    except Exception as e:
        logger.error(f"Error publishing results: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def courses_available(request):
    try:
        student = AuthService.get_student_profile(request.user)
        if not student: return Response({"detail": "No profile"}, status=404)
        
        # In a real app, this would filter out already registered courses
        courses = Course.objects.all()
        # Ensure we return data compatible with AcademicCourse in Kotlin
        data = []
        for c in courses:
            data.append({
                "id": c.id,
                "code": c.code,
                "name": c.name,
                "credit_units": c.credit_units,
                "department_name": c.program.department.name if c.program and c.program.department else None,
                "description": c.description
            })
        return Response(data)
    except Exception as e:
        logger.error(f"Error fetching available courses: {e}")
        return Response({"detail": str(e)}, status=500)

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def enroll_courses(request):
    try:
        student = AuthService.get_student_profile(request.user)
        if not student: return Response({"detail": "No profile"}, status=404)
        
        course_ids = request.data.get('courseIds', [])
        if not course_ids:
            return Response({"detail": "No courses provided"}, status=400)
            
        semester = Semester.objects.order_by('-end_date').first()
        if not semester:
            return Response({"detail": "No active semester"}, status=400)
            
        enrolled = []
        for cid in course_ids:
            try:
                course = Course.objects.get(id=cid)
                reg, created = CourseRegistration.objects.get_or_create(
                    student=student,
                    course=course,
                    semester=semester
                )
                if created:
                    enrolled.append(course.code)
            except Course.DoesNotExist:
                pass
                
        return Response({
            "success": True, 
            "message": f"Successfully enrolled in {len(enrolled)} courses",
            "enrolled_courses": enrolled
        })
    except Exception as e:
        logger.error(f"Error during enrollment: {e}")
        return Response({"detail": str(e)}, status=500)
@api_view(["GET"])
@permission_classes([IsAuthenticated])
def student_enrolled_courses(request):
    try:
        student = AuthService.get_student_profile(request.user)
        if not student: return Response({"detail": "No student profile found"}, status=404)
        
        # Fetch current semester registrations
        semester = Semester.objects.order_by('-end_date').first()
        registrations = CourseRegistration.objects.filter(
            student=student,
            semester=semester
        ).select_related('course', 'course__program__department')
        
        data = []
        for reg in registrations:
            c = reg.course
            data.append({
                "id": c.id,
                "code": c.code,
                "name": c.name,
                "credit_units": c.credit_units,
                "department_name": c.program.department.name if c.program and c.program.department else None,
            })
        return Response(data)
    except Exception as e:
        logger.error(f"Error fetching enrolled courses: {e}")
        return Response({"detail": str(e)}, status=500)
