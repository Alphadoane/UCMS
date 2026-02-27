import os
import django
import sys

# Set up Django environment
sys.path.append(os.getcwd())
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.voting.models import Election, Candidate, Vote
from student_api.academics.models import Student
from student_api.core.models import User
from django.utils import timezone

def verify_voting_logic():
    print("--- Verifying Voting Logic ---")
    
    # 1. Setup Test Data
    from student_api.academics.models import Program, Faculty, Department
    faculty, _ = Faculty.objects.get_or_create(name="Engineering")
    dept, _ = Department.objects.get_or_create(faculty=faculty, name="Computer Science")
    program, _ = Program.objects.get_or_create(department=dept, code="BCS", defaults={"name": "Computer Science", "duration_years": 4})
    
    user, _ = User.objects.get_or_create(email="test_student@example.com", defaults={"first_name": "Test", "last_name": "Student"})
    student, _ = Student.objects.get_or_create(user=user, defaults={"admission_number": "TEST001", "program": program, "admission_date": timezone.now().date(), "status": "active"})
    
    admin_user, _ = User.objects.get_or_create(email="admin@example.com", defaults={"is_staff": True, "first_name": "Admin"})
    
    election = Election.objects.create(title="Student Council 2026", description="Annual election")
    c1 = Candidate.objects.create(election=election, name="Candidate A", manifesto="Vote for me!")
    c2 = Candidate.objects.create(election=election, name="Candidate B", manifesto="I will deliver!")
    
    print(f"Election created: {election.title}")
    
    # 2. Test Voting
    print("Casting vote for Candidate A...")
    Vote.objects.create(election=election, candidate=c1, student=student)
    
    # 3. Verify Vote Count
    from django.db.models import Count
    results = Candidate.objects.filter(election=election).annotate(vote_count=Count('votes'))
    for c in results:
        print(f"Candidate: {c.name}, Votes: {c.vote_count}")
    
    # 4. Verify Duplicate Vote Constraint
    print("Testing duplicate vote constraint...")
    try:
        Vote.objects.create(election=election, candidate=c2, student=student)
        print("FAILED: Student was able to vote twice!")
    except Exception as e:
        print(f"SUCCESS: Could not vote twice. Error: {e}")
        
    # 5. Clean up
    Vote.objects.filter(student=student).delete()
    Candidate.objects.filter(election=election).delete()
    election.delete()
    print("--- Verification Complete ---")

if __name__ == "__main__":
    verify_voting_logic()
