from django.db.models import Count
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from student_api.voting.models import Election, Candidate, Vote
from student_api.academics.models import Student
from student_api.core.serializers_voting import ElectionSerializer, CandidateSerializer, VoteSerializer

@api_view(["GET", "POST"])
@permission_classes([IsAuthenticated])
def voting_elections(request):
    if request.method == "POST":
        if not request.user.is_staff and not request.user.is_superuser:
            return Response({"error": "Unauthorized"}, status=403)
            
        title = request.data.get("title")
        description = request.data.get("description", "")
        end_date = request.data.get("end_date")
        
        if not title:
            return Response({"error": "Title required"}, status=400)
            
        election = Election.objects.create(
            title=title, 
            description=description,
            end_date=end_date,
            is_active=True
        )
        serializer = ElectionSerializer(election, context={'request': request})
        return Response(serializer.data, status=201)
        
    # GET Logic
    if request.user.is_staff or request.user.is_superuser:
        elections = Election.objects.all()
    else:
        elections = Election.objects.filter(is_active=True)
    serializer = ElectionSerializer(elections, many=True, context={'request': request})
    return Response({"items": serializer.data})

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def voting_cast_vote(request, election_id):
    try:
        student = Student.objects.get(user=request.user)
    except Student.DoesNotExist:
        return Response({"error": "Only students can vote"}, status=403)
    
    try:
        election = Election.objects.get(id=election_id, is_active=True)
    except Election.DoesNotExist:
        return Response({"error": "Election not found or inactive"}, status=404)
        
    candidate_id = request.data.get("candidate_id")
    if not candidate_id:
        return Response({"error": "Candidate ID required"}, status=400)
        
    try:
        candidate = Candidate.objects.get(id=candidate_id, election=election)
    except Candidate.DoesNotExist:
        return Response({"error": "Invalid candidate for this election"}, status=400)
        
    if Vote.objects.filter(election=election, student=student).exists():
        return Response({"error": "You have already voted in this election"}, status=400)
        
    Vote.objects.create(election=election, candidate=candidate, student=student)
    return Response({"status": "Vote cast successfully", "election_id": election_id, "candidate": candidate.name})

@api_view(["POST"])
@permission_classes([IsAuthenticated])
def add_candidate(request, election_id=None):
    # For now, allow any staff or superuser
    if not request.user.is_staff and not request.user.is_superuser:
         return Response({"error": "Unauthorized"}, status=403)
         
    election_id = election_id or request.data.get("election_id")
    try:
        election = Election.objects.get(id=election_id)
    except Election.DoesNotExist:
        return Response({"error": "Election not found"}, status=404)
        
    name = request.data.get("name")
    manifesto = request.data.get("manifesto", "")
    
    if not name:
        return Response({"error": "Candidate name required"}, status=400)
        
    candidate = Candidate.objects.create(election=election, name=name, manifesto=manifesto)
    return Response({"status": "Candidate Added", "id": candidate.id})

@api_view(["GET"])
@permission_classes([IsAuthenticated])
def election_results(request, election_id):
    try:
        election = Election.objects.get(id=election_id)
    except Election.DoesNotExist:
        return Response({"error": "Election not found"}, status=404)
        
    candidates = Candidate.objects.filter(election=election).annotate(vote_count=Count('votes'))
    serializer = CandidateSerializer(candidates, many=True)
    
    return Response({
        "election": election.title,
        "results": serializer.data,
        "total_votes": Vote.objects.filter(election=election).count()
    })
