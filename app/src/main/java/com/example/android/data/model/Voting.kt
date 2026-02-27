package com.example.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Election(
    val id: Int,
    val title: String,
    val description: String = "",
    val start_date: String,
    val end_date: String? = null,
    val is_active: Boolean,
    val candidates: List<Candidate> = emptyList(),
    val has_voted: Boolean = false
)

@Serializable
data class Candidate(
    val id: Int,
    val name: String,
    val manifesto: String = "",
    val vote_count: Int = 0
)

@Serializable
data class VoteRequest(
    val candidate_id: Int
)

@Serializable
data class ElectionRequest(
    val title: String,
    val description: String,
    val start_date: String,
    val end_date: String? = null,
    val is_active: Boolean = true
)

@Serializable
data class CandidateRequest(
    val name: String,
    val manifesto: String
)

@Serializable
data class ElectionResultsResponse(
    val results: List<Candidate>
)

@Serializable
data class ElectionListResponse(
    val items: List<Election>
)
