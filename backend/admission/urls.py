from django.urls import path
from . import views

urlpatterns = [
    path("programs", views.get_programs),
    path("apply", views.apply_admission),
    path("upload/<path:application_id>", views.upload_document),
    path("submit/<path:application_id>", views.submit_application),
    path("status", views.check_status),
    
    # Admin
    path("list", views.get_applications),
    path("programs/create", views.admin_create_program),
    path("programs/<int:program_id>", views.admin_manage_program),
    path("verify/<int:doc_id>", views.verify_document),
    path("phase/<path:application_id>", views.update_phase),
    path("enroll/<path:application_id>", views.enroll_student),
]
