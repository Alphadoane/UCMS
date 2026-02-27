from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, IsAdminUser
from django.db.models import Sum, Count, Avg
from django.utils import timezone
from datetime import timedelta
from student_api.core.models import User
from student_api.academics.models import Student, CourseRegistration, Grade, CourseWork, Submission, ZoomRoom
from student_api.support.models import Complaint
from student_api.finance.models import Payment, FeeAccount, Invoice
from student_api.voting.models import Election, Vote

@api_view(["GET"])
@permission_classes([IsAdminUser])
def get_admin_reports(request):
    # 1. User Stats
    total_users = User.objects.count()
    
    # 2. Complaint Stats
    total_complaints = Complaint.objects.count()
    resolved_complaints = Complaint.objects.filter(status='resolved').count()
    
    # 3. Financial Stats
    total_revenue = Payment.objects.filter(status='SUCCESS').aggregate(Sum('amount'))['amount__sum'] or 0.0
    total_billed = Invoice.objects.aggregate(Sum('amount'))['amount__sum'] or 0.0
    
    collection_rate = 0.0
    if total_billed > 0:
        collection_rate = (float(total_revenue) / float(total_billed)) * 100.0
    
    # 4. Voting Turnout (based on latest active election)
    active_election = Election.objects.filter(is_active=True).first()
    turnout = 0.0
    if active_election:
        vote_count = Vote.objects.filter(election=active_election).count()
        student_count = Student.objects.count()
        if student_count > 0:
            turnout = (vote_count / student_count) * 100.0
            
    # 5. Weekly Activity Engagement
    one_week_ago = timezone.now() - timedelta(days=7)
    active_students_weekly = Submission.objects.filter(submitted_at__gte=one_week_ago).values('student').distinct().count()
    
    # 6. Module Engagement Breakdown
    module_engagement = {
        "Virtual Campus": ZoomRoom.objects.count(),
        "Finance": Payment.objects.count(),
        "Support": Complaint.objects.count(),
        "Voting": Vote.objects.count(),
        "Academics": CourseRegistration.objects.count()
    }
    
    # 7. Average Response Time
    resolved_qs = Complaint.objects.filter(status='resolved')
    count_resolved = resolved_qs.count()
    avg_hours = 0.0
    if count_resolved > 0:
        total_seconds = 0
        for c in resolved_qs:
            diff = c.updated_at - c.created_at
            total_seconds += diff.total_seconds()
        avg_hours = (total_seconds / count_resolved) / 3600.0

    return Response({
        "totalUsers": total_users,
        "totalComplaints": total_complaints,
        "resolvedComplaints": resolved_complaints,
        "totalRevenue": float(total_revenue),
        "votingTurnout": round(turnout, 2),
        "activeStudentsWeekly": active_students_weekly,
        "moduleEngagement": module_engagement,
        "collectionRate": round(collection_rate, 2),
        "averageResponseTimeHours": round(avg_hours, 1)
    })

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_student_analytics(request):
    try:
        student = Student.objects.get(user=request.user)
    except Student.DoesNotExist:
        return Response({"error": "Student profile not found"}, status=404)

    # 1. GPA Trend
    # Aggregating grades by semester
    grades = Grade.objects.filter(registration__student=student).select_related('registration__semester')
    semester_gpa = {}
    for g in grades:
        sem_name = g.registration.semester.name
        if sem_name not in semester_gpa:
            semester_gpa[sem_name] = []
        semester_gpa[sem_name].append(float(g.score))
    
    gpa_trend = []
    for sem, scores in semester_gpa.items():
        avg_score = sum(scores) / len(scores)
        # Convert score to GPA (e.g. 0-100 to 0-4.0)
        gpa = (avg_score / 100.0) * 4.0
        gpa_trend.append({"semester": sem, "gpa": round(gpa, 2)})

    # 2. Financial Summary
    try:
        fee_account = FeeAccount.objects.get(student=student)
        balance = float(fee_account.balance)
    except FeeAccount.DoesNotExist:
        balance = 0.0
        
    total_billed = float(Invoice.objects.filter(student=student).aggregate(Sum('amount'))['amount__sum'] or 0.0)
    total_paid = float(Payment.objects.filter(student=student, status='SUCCESS').aggregate(Sum('amount'))['amount__sum'] or 0.0)

    # 3. Attendance Rate
    # No attendance model exists yet, so default to 0.0 instead of dummy data
    attendance_rate = 0.0
    
    # 4. Assignment Completion
    # Count submissions vs total coursework available for student's courses in current semesters
    total_assigned = CourseWork.objects.filter(course__courseregistration__student=student).distinct().count()
    total_submitted = Submission.objects.filter(student=student).count()
    
    completion_rate = 0.0
    if total_assigned > 0:
        completion_rate = (total_submitted / total_assigned) * 100.0

    # 5. Payment Consistency Score
    payment_consistency_score = 0
    if total_billed > 0:
        # Simple ratio of paid vs billed capped at 100
        ratio = (total_paid / total_billed) * 100.0
        payment_consistency_score = int(min(ratio, 100.0))

    return Response({
        "studentId": str(student.user.id),
        "gpaTrend": gpa_trend,
        "financialStatus": {
            "totalBilled": total_billed,
            "totalPaid": total_paid,
            "balance": balance,
            "paymentConsistencyScore": payment_consistency_score
        },
        "attendanceRate": attendance_rate,
        "assignmentCompletion": round(completion_rate, 2)
    })
