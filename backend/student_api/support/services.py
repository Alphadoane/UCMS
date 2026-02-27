from student_api.support.models import Complaint, ComplaintTimeline
from student_api.academics.models import Lecture

class UCMSService:
    @staticmethod
    def log_event(complaint, event_type, description, user=None):
        """Helper to log events to the complaint timeline"""
        ComplaintTimeline.objects.create(
            complaint=complaint,
            event_type=event_type,
            description=description,
            user=user
        )

    @staticmethod
    def get_assigned_lecturer(course):
        """Retrieves the primary lecturer assigned to a course unit"""
        # For now, we take the first lecturer assigned in the 'Lecture' schedule
        lecture = Lecture.objects.filter(course=course).first()
        if lecture:
            return lecture.lecturer
        return None

    @staticmethod
    def route_complaint(complaint, user):
        """Initial routing and logging for a new complaint"""
        UCMSService.log_event(
            complaint=complaint,
            event_type='submitted',
            description=f"Complaint formally lodged by student {complaint.student.admission_number}.",
            user=user
        )
        
        # Additional routing logic (e.g. notifications) would go here
