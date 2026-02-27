package com.example.android

object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ACADEMICS = "academics"
    const val FINANCE = "finance"
    const val VIRTUAL_CAMPUS = "virtual_campus"
    const val LIBRARY = "library"
    const val CHAT = "chat"

    const val VOTING = "voting"
    // Academics subroutes
    const val ACADEMICS_COURSE_REG = "academics_course_registration"
    const val ACADEMICS_COURSE_WORK = "academics_course_work"
    const val ACADEMICS_EXAM_CARD = "academics_exam_card"
    const val ACADEMICS_EXAM_AUDIT = "academics_exam_audit"
    const val ACADEMICS_EXAM_RESULT = "academics_exam_result"
    const val ACADEMICS_ACADEMIC_LEAVE = "academics_academic_leave"
    const val ACADEMICS_CLEARANCE = "academics_clearance"
    const val ACADEMICS_INSIGHTS = "academics_insights"
    const val ACADEMICS_RESULT_SLIP = "academics_result_slip"

    // Finance subroutes
    const val FINANCE_FEE_PAYMENT = "finance_fee_payment"
    const val FINANCE_VIEW_BALANCE = "finance_view_balance"
    const val FINANCE_RECEIPTS = "finance_receipts"

    // Virtual Campus subroutes
    const val VC_DASHBOARD = "vc_dashboard"
    const val VC_MY_COURSES = "vc_my_courses"
    const val VC_LECTURES = "vc_lectures"
    const val VC_ZOOM_ROOMS = "vc_zoom_rooms"
    const val VC_UNIT_CONTENT = "vc_unit_content/{courseId}"
    const val VC_MEETING_ROOM = "vc_meeting_room/{roomId}" // New Route

    // Complaint subroutes
    // Complaint subroutes
    const val COMPLAINTS = "complaints"
    
    // Health
    const val HEALTH = "health"
    
    // Admin Routes
    const val ADMIN_USER_MGMT = "admin_user_mgmt"
    const val ADMIN_ELECTION = "admin_election"
    const val ADMIN_CAMPUS_LIFE = "admin_campus_life"
    const val ADMIN_HEALTH = "admin_health"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_RESULT_MGMT = "admin_result_mgmt"

    // Staff Routes
    const val STAFF_COURSES = "staff_courses"
    const val STAFF_STUDENT_LOOKUP = "staff_student_lookup"

    // Student 360 / Analytics
    const val STUDENT_ANALYTICS = "student_performance_analytics"
}
