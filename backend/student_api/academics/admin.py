from django.contrib import admin
from .models import (
    Faculty, Department, Program, Course, AcademicYear, Semester, 
    Student, Lecturer, CourseRegistration, Grade, CourseWork, Submission,
    ExamSession, Lecture
)

# --- Organization & Academic ---
@admin.register(Faculty)
class FacultyAdmin(admin.ModelAdmin):
    list_display = ('name',)
    search_fields = ('name',)

@admin.register(Department)
class DepartmentAdmin(admin.ModelAdmin):
    list_display = ('name', 'faculty')
    list_filter = ('faculty',)
    search_fields = ('name',)

@admin.register(Program)
class ProgramAdmin(admin.ModelAdmin):
    list_display = ('code', 'name', 'department', 'category', 'duration_years')
    list_filter = ('category', 'department')
    search_fields = ('code', 'name')

@admin.register(Course)
class CourseAdmin(admin.ModelAdmin):
    list_display = ('code', 'name', 'program', 'credit_units')
    list_filter = ('program',)
    search_fields = ('code', 'name')

@admin.register(AcademicYear)
class AcademicYearAdmin(admin.ModelAdmin):
    list_display = ('year_label',)

@admin.register(Semester)
class SemesterAdmin(admin.ModelAdmin):
    list_display = ('name', 'academic_year', 'start_date', 'end_date')
    list_filter = ('academic_year',)

# --- Profiles ---
@admin.register(Student)
class StudentAdmin(admin.ModelAdmin):
    list_display = ('admission_number', 'user', 'program', 'status', 'admission_date')
    search_fields = ('admission_number', 'user__email', 'user__first_name', 'user__last_name')
    list_filter = ('status', 'program', 'admission_date')
    
    fieldsets = (
        ('Identity', {
            'fields': ('user', 'admission_number')
        }),
        ('Academic Info', {
            'fields': ('program', 'admission_date', 'status')
        }),
    )

@admin.register(Lecturer)
class LecturerAdmin(admin.ModelAdmin):
    list_display = ('employee_id', 'user', 'department', 'employment_type')
    search_fields = ('employee_id', 'user__email')
    list_filter = ('department', 'employment_type')

# --- Course Registration & Assessment ---
@admin.register(CourseRegistration)
class CourseRegistrationAdmin(admin.ModelAdmin):
    list_display = ('student', 'course', 'semester', 'registered_at')
    list_filter = ('semester', 'course')
    search_fields = ('student__admission_number', 'student__user__email')

@admin.register(Grade)
class GradeAdmin(admin.ModelAdmin):
    list_display = ('registration', 'score', 'grade', 'approved', 'approved_at')
    list_filter = ('approved', 'grade')
    search_fields = ('registration__student__admission_number',)

@admin.register(CourseWork)
class CourseWorkAdmin(admin.ModelAdmin):
    list_display = ('title', 'course', 'lecturer', 'due_date', 'max_marks')
    list_filter = ('course', 'lecturer')
    search_fields = ('title',)

@admin.register(Submission)
class SubmissionAdmin(admin.ModelAdmin):
    list_display = ('course_work', 'student', 'submitted_at', 'score')
    list_filter = ('submitted_at',)
    search_fields = ('student__admission_number', 'course_work__title')

# --- Exams & Lectures ---
@admin.register(ExamSession)
class ExamSessionAdmin(admin.ModelAdmin):
    list_display = ('course', 'semester', 'date', 'venue')
    list_filter = ('semester', 'date')

@admin.register(Lecture)
class LectureAdmin(admin.ModelAdmin):
    list_display = ('course', 'day_of_week', 'start_time', 'end_time', 'venue')
    list_filter = ('day_of_week', 'venue')
