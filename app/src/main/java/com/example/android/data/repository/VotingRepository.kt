package com.example.android.data.repository

import android.content.Context
import com.example.android.data.model.CandidateRequest
import com.example.android.data.model.Election
import com.example.android.data.model.ElectionRequest
import com.example.android.data.model.VoteRequest
import com.example.android.data.model.ElectionResultsResponse
import com.example.android.data.model.Candidate
import com.example.android.data.network.ApiService
import com.example.android.data.network.NetworkModule
import retrofit2.Response

class VotingRepository(private val context: Context) {
    private val api: ApiService by lazy { NetworkModule.createApiService(context) }

    suspend fun getElections(): Response<com.example.android.data.model.ElectionListResponse> {
        return api.getElections()
    }

    suspend fun createElection(request: ElectionRequest): Response<Election> {
        return api.createElection(request)
    }

    suspend fun addCandidate(electionId: Int, request: CandidateRequest): Response<Candidate> {
        return api.addCandidate(electionId, request)
    }

    suspend fun castVote(electionId: Int, request: VoteRequest): Response<Unit> {
        return api.castVote(electionId, request)
    }

    suspend fun getElectionResults(electionId: Int): Response<ElectionResultsResponse> {
        return api.getElectionResults(electionId)
    }
}
