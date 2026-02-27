package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService

class VotingRepository(private val apiService: SharedApiService) {
    suspend fun getElections(): Result<List<Election>> = apiService.getElections()
    
    suspend fun castVote(electionId: Int, candidateId: Int): Result<VoteResponse> = 
        apiService.castVote(electionId, candidateId)
        
    suspend fun getElectionResults(electionId: Int): Result<ElectionResultsResponse> = 
        apiService.getElectionResults(electionId)

    // Admin/Staff methods
    suspend fun createElection(request: ElectionRequest): Result<Election> = 
        apiService.createElection(request)
        
    suspend fun addCandidate(electionId: Int, request: CandidateRequest): Result<Candidate> = 
        apiService.addCandidate(electionId, request)
}
