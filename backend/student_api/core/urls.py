from django.urls import path
from rest_framework.routers import DefaultRouter
from . import views
from student_api.support.api.health_campus import CampusLifeViewSet, AppointmentViewSet, EmergencyAlertViewSet

router = DefaultRouter()
router.register(r'support/campus-life', CampusLifeViewSet, basename='campus-life')
router.register(r'support/appointments', AppointmentViewSet, basename='appointments')
router.register(r'support/emergency', EmergencyAlertViewSet, basename='emergency')

urlpatterns = [
    path("auth/login", views.login_view),
    path("auth/register", views.create_user), # Admin Only
    path("auth/impersonate", views.impersonate_user),
    path("profile/me", views.profile_me),
    path("profile/update", views.profile_update),
    path("profile/avatar", views.upload_avatar),
    
    # Academics
    path("academics/course-registration", views.academics_course_registration),
    path("academics/course-work", views.academics_course_work),
    path("academics/exam-card", views.academics_exam_card),
    path("academics/result", views.academics_exam_result),
    path("academics/result-slip", views.get_result_slip),
    path("academics/result-slip/download", views.download_result_slip),
    path("academics/publish", views.publish_results),
    
    # Finance
    path("finance/view-balance", views.finance_balance),
    path("finance/receipts/", views.finance_statement),
    path("finance/receipts", views.finance_statement),
    path("finance/stk-push", views.finance_stk_push),
    path("finance/mpesa-callback", views.finance_mpesa_callback),
    path("finance/paystack-initialize", views.finance_paystack_initialize),
    path("finance/paystack-webhook", views.finance_paystack_webhook),
    path("admin/finance/transactions", views.admin_all_transactions),
    
    # Support
    path("support/tickets", views.support_tickets),
    path("support/tickets/<int:ticket_id>", views.support_ticket_detail),
    path("support/tickets/<int:ticket_id>/status", views.update_ticket_status),
    
    # VC - Fixed endpoint to match frontend expectation
    path("virtual/zoom-rooms", views.vc_zoom_rooms),
    path("virtual/agora-token", views.get_agora_token),
    path("academics/timetable", views.get_timetable),
    path("admin/academics/allocate", views.admin_allocate_lecture),
    path("admin/academics/allocate/options", views.admin_allocation_options),
    
    # CourseWork & Submissions
    path("academics/lecturer-course-work", views.lecturer_course_work), # Fixed to match frontend expectation
    path("academics/assignments/<int:work_id>/submit", views.student_submit_work),
    path("staff/courses", views.staff_courses), # New endpoint

    # Missing academic endpoints
    path("academics/exam-audit", views.academics_exam_audit),
    path("academics/academic-leave", views.academics_academic_leave),
    path("academics/clearance", views.academics_clearance),
    
    # Voting
    path("voting/elections", views.elections_list),
    path("voting/elections/<int:election_id>/candidates", views.add_candidate),
    path("voting/elections/<int:election_id>/vote", views.cast_vote),
    path("voting/elections/<int:election_id>/results", views.election_results),
    
    # Admin Stats
    path("admin/stats", views.admin_stats),
    path("admin/reports", views.get_admin_reports),
    path("student/analytics", views.get_student_analytics),
    
    
    # Broadcasts
    path("admin/broadcast/send", views.send_broadcast),

    # Password Management
    path("auth/password/change", views.change_password),
    path("auth/password/reset-request", views.request_password_reset),
    path("auth/password/reset-confirm", views.verify_otp_and_reset),
    path("admin/users/<uuid:user_id>/reset-password", views.reset_password),
    
    # User Management
    path("users", views.admin_users_list),
]

urlpatterns += router.urls
