package com.cs22.example.smarthire.firebase

import com.cs22.example.smarthire.model.CandidateProfileResponse
import com.cs22.example.smarthire.network.RetrofitClient

/**
 * Repository for Recruiters to search and fetch Candidate Profiles using AI via Django.
 */
object CandidateRepository {

    suspend fun searchCandidatesWithAlgorithm(
        skills: List<String>,
        minExperience: Int,
        degree: String
    ): Result<List<CandidateProfileResponse>> = runCatching {
        RetrofitClient.api.searchCandidates(
            skills = if (skills.isNotEmpty()) skills.joinToString(",") else null,
            degree = degree.ifEmpty { null },
            experience = if (minExperience > 0) minExperience.toString() else null
        )
    }
}
