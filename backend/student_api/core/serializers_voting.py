from rest_framework import serializers
from student_api.voting.models import Election, Candidate, Vote

class CandidateSerializer(serializers.ModelSerializer):
    vote_count = serializers.IntegerField(read_only=True)
    
    class Meta:
        model = Candidate
        fields = ['id', 'name', 'manifesto', 'vote_count']

class ElectionSerializer(serializers.ModelSerializer):
    candidates = CandidateSerializer(many=True, read_only=True)
    has_voted = serializers.SerializerMethodField()
    
    class Meta:
        model = Election
        fields = ['id', 'title', 'description', 'start_date', 'end_date', 'is_active', 'candidates', 'has_voted']
        
    def get_has_voted(self, obj):
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            try:
                from student_api.academics.models import Student
                student = Student.objects.get(user=request.user)
                return Vote.objects.filter(election=obj, student=student).exists()
            except Exception:
                return False
        return False

class VoteSerializer(serializers.ModelSerializer):
    class Meta:
        model = Vote
        fields = ['id', 'election', 'candidate', 'student', 'casted_at']
        read_only_fields = ['student', 'casted_at']
