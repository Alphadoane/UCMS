from .auth import (
    login_view, impersonate_user, profile_me, profile_update, upload_avatar,
    change_password, request_password_reset, verify_otp_and_reset, reset_password
)
from .admin import (
    admin_create_user as create_user, 
    admin_stats, 
    send_broadcast,
    admin_users_list
)
from .academics import (
    academics_course_registration, 
    academics_exam_card, 
    academics_exam_result,
    get_timetable,
    academics_course_work,
    admin_allocate_lecture,
    admin_allocation_options,
    lecturer_course_work,
    student_submit_work,
    academics_exam_audit,
    academics_academic_leave,
    academics_clearance,
    staff_courses,
    staff_course_students,
    staff_course_work,
    staff_course_materials,
    get_result_slip, 
    download_result_slip, 
    publish_results,
    courses_available,
    enroll_courses
)
from .finance import (
    finance_balance, 
    finance_statement,
    finance_stk_push,
    finance_mpesa_callback,
    finance_paystack_initialize,
    finance_paystack_webhook,
    admin_finance_students,
    admin_student_transactions,
    admin_all_transactions
)
from .support import support_tickets, support_ticket_detail, update_ticket_status
from .voting import (
    voting_elections, 
    add_candidate, 
    voting_cast_vote as cast_vote, 
    election_results
)
from .virtual_campus import vc_zoom_rooms, get_agora_token, delete_zoom_room
from .analytics import get_admin_reports, get_student_analytics
from .academics import get_result_slip, download_result_slip, publish_results
