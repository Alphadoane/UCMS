from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views
from .api import academics
from student_api.finance import views as finance_views
from student_api.support.api.health_campus import (
    CampusLifeViewSet, AppointmentViewSet, EmergencyAlertViewSet, admin_health_stats
)

router = DefaultRouter()
router.register(r'support/campus-life', CampusLifeViewSet, basename='campus-life')
router.register(r'support/appointments', AppointmentViewSet, basename='appointments')
router.register(r'support/emergency', EmergencyAlertViewSet, basename='emergency')

urlpatterns = [
    path("auth/login", views.login_view),
    path("auth/login/", views.login_view),
    path("auth/register", views.create_user), # Admin Only
    path("auth/register/", views.create_user),
    path("auth/impersonate", views.impersonate_user),
    path("profile/me", views.profile_me),
    path("profile/me/", views.profile_me),
    path("profile/update", views.profile_update),
    path("profile/update/", views.profile_update),
    path("profile/avatar", views.upload_avatar),
    path("profile/avatar/", views.upload_avatar),
    
    # Academics
    path("academics/course-registration", views.academics_course_registration),
    path("academics/course-registration/", views.academics_course_registration),
    path("academics/courses/available", views.courses_available),
    path("academics/courses/enrolled", academics.student_enrolled_courses),
    path("academics/enroll", views.enroll_courses),
    path("academics/course-work", views.academics_course_work),
    path("academics/course-work/", views.academics_course_work),
    path("academics/exam-card", views.academics_exam_card),
    path("academics/result", views.academics_exam_result),
    path("academics/result/", views.academics_exam_result),
    path("academics/result-slip", views.get_result_slip),
    path("academics/result-slip/download", views.download_result_slip),
    path("academics/publish", views.publish_results),
    
    # Finance
    path("finance/view-balance", views.finance_balance),
    path("finance/view-balance/", views.finance_balance),
    path("finance/receipts", views.finance_statement),
    path("finance/receipts/", views.finance_statement),
    
    # M-Pesa Endpoints (Pointed to unified finance app logic)
    path("finance/stk-push", finance_views.initiate_stk_push), 
    path("finance/stk-push/", finance_views.initiate_stk_push), 
    path("finance/mpesa/stk-push", finance_views.initiate_stk_push),
    path("finance/mpesa/stk-push/", finance_views.initiate_stk_push),
    path("finance/mpesa-callback", finance_views.mpesa_callback),
    path("finance/mpesa-callback/", finance_views.mpesa_callback),
    path("finance/paystack-initialize", views.finance_paystack_initialize),
    path("finance/paystack-webhook", views.finance_paystack_webhook),
    path("admin/finance/transactions", views.admin_all_transactions),
    path("admin/finance/students", views.admin_finance_students),
    path("admin/finance/transactions/<uuid:student_id>", views.admin_student_transactions),
    
    # Support
    path("support/tickets", views.support_tickets),
    path("support/tickets/", views.support_tickets),
    path("support/tickets/<int:ticket_id>", views.support_ticket_detail),
    path("support/tickets/<int:ticket_id>/", views.support_ticket_detail),
    path("support/tickets/<int:ticket_id>/status", views.update_ticket_status),
    path("support/tickets/<int:ticket_id>/status/", views.update_ticket_status),
    
    # VC - Fixed endpoint to match frontend expectation
    path("virtual/zoom-rooms", views.vc_zoom_rooms),
    path("virtual/zoom-rooms/", views.vc_zoom_rooms),
    path("virtual/zoom-rooms/<int:room_id>", views.delete_zoom_room),
    path("virtual/agora-token", views.get_agora_token),
    path("virtual/agora-token/", views.get_agora_token),
    path("academics/timetable", views.get_timetable),
    path("academics/timetable/", views.get_timetable),
    path("admin/academics/allocate", views.admin_allocate_lecture),
    path("admin/academics/allocate/", views.admin_allocate_lecture),
    path("admin/academics/allocate/options", views.admin_allocation_options),
    path("admin/academics/allocate/options/", views.admin_allocation_options),
    
    # CourseWork & Submissions
    path("academics/lecturer-course-work", views.lecturer_course_work), # Fixed to match frontend expectation
    path("academics/lecturer-course-work/", views.lecturer_course_work),
    path("academics/assignments/<int:work_id>/submit", views.student_submit_work),
    path("staff/courses", views.staff_courses), # New endpoint
    path("staff/courses/", views.staff_courses),
    path("staff/courses/<int:course_id>/students", views.staff_course_students), 
    path("staff/courses/<int:course_id>/work", views.staff_course_work),
    path("staff/courses/<int:course_id>/materials", views.staff_course_materials),

    # Missing academic endpoints
    path("academics/exam-audit", views.academics_exam_audit),
    path("academics/exam-audit/", views.academics_exam_audit),
    path("academics/academic-leave", views.academics_academic_leave),
    path("academics/academic-leave/", views.academics_academic_leave),
    path("academics/clearance", views.academics_clearance),
    path("academics/clearance/", views.academics_clearance),
    
    # Voting
    path("voting/elections", views.voting_elections),
    path("voting/elections/", views.voting_elections),
    path("voting/elections/<int:election_id>/candidates", views.add_candidate),
    path("voting/elections/<int:election_id>/candidates/", views.add_candidate),
    path("voting/elections/<int:election_id>/vote", views.cast_vote),
    path("voting/elections/<int:election_id>/vote/", views.cast_vote),
    path("voting/elections/<int:election_id>/results", views.election_results),
    path("voting/elections/<int:election_id>/results/", views.election_results),
    
    # Admin Stats
    path("admin/stats", views.admin_stats),
    path("admin/stats/", views.admin_stats),
    path("admin/reports", views.get_admin_reports),
    path("admin/reports/", views.get_admin_reports),
    path("student/analytics", views.get_student_analytics),
    path("student/analytics/", views.get_student_analytics),
    
    
    # Broadcasts
    path("admin/broadcast/send", views.send_broadcast),

    # Password Management
    path("auth/password/change", views.change_password),
    path("auth/password/change/", views.change_password),
    path("auth/password/reset-request", views.request_password_reset),
    path("auth/password/reset-request/", views.request_password_reset),
    path("auth/password/reset-confirm", views.verify_otp_and_reset),
    path("auth/password/reset-confirm/", views.verify_otp_and_reset),
    path("admin/users/<uuid:user_id>/reset-password", views.reset_password),
    
    # User Management
    path("users", views.admin_users_list),
    
    # Health & Emergency Admin
    path("admin/health/stats", admin_health_stats),
    
    # Library
    path("", include("student_api.library.urls")),
]

urlpatterns += router.urls
