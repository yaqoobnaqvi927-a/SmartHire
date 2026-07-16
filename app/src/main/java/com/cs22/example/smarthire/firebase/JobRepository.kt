package com.cs22.example.smarthire.firebase

import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Django REST API repository for Jobs and Applications.
 * All methods return Flows or suspend functions matching original signatures.
 */
object JobRepository {

    // ── Real-time job feed (ordered by date) ──────────────────────────────
    fun getJobsFlow(): Flow<List<DjangoJob>> = flow {
        try {
            val jobs = RetrofitClient.api.getJobs()
            emit(jobs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // ── Recruiter's own job postings (real-time) ──────────────────────────
    fun getMyPostingsFlow(): Flow<List<DjangoJob>> = flow {
        try {
            val jobs = RetrofitClient.api.getMyPostings()
            emit(jobs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // ── Post a new job ────────────────────────────────────────────────────
    suspend fun postJob(
        title: String,
        company: String,
        location: String,
        locationType: String,
        department: String,
        description: String,
        salaryRange: String,
        skills: List<String>,
        experienceLevel: String
    ): Result<String> = runCatching {
        val jobMap = mapOf(
            "title" to title,
            "company" to company,
            "location" to location,
            "job_type" to locationType.lowercase(), // onsite, remote, hybrid
            "description" to description,
            "salary_range" to salaryRange,
            "required_skills_json" to skills,
            "min_experience" to (experienceLevel.toIntOrNull() ?: 0)
        )
        val response = RetrofitClient.api.postJob(jobMap)
        if (response.isSuccessful) {
            response.body()?.id ?: throw Exception("Response body empty")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to post job")
        }
    }

    // ── Toggle job active/paused ──────────────────────────────────────────
    suspend fun toggleJobStatus(jobId: String, currentStatus: String): Result<Unit> = runCatching {
        RetrofitClient.api.toggleJobStatus(jobId)
        Unit
    }

    // ── Apply to a job ────────────────────────────────────────────────────
    suspend fun applyToJob(jobId: String, coverLetter: String = ""): Result<String> = runCatching {
        // Django obtains student (candidate) profile from authenticated user session
        val request = JobApplicationRequest(job = jobId.toInt())
        val response = RetrofitClient.api.submitApplication(request)
        response.id
    }

    // ── Get applications for a candidate (real-time) ──────────────────────
    fun getMyApplicationsFlow(): Flow<List<Application>> = flow {
        try {
            val apps = RetrofitClient.api.getApplications()
            emit(apps)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // ── AI Powered Search (TF-IDF + Skill Overlap) via Django API ─────────
    suspend fun searchJobsWithAlgorithm(
        query: String,
        skills: List<String>,
        minExperience: Int,
        location: String
    ): Result<List<DjangoJob>> = runCatching {
        RetrofitClient.api.getJobs(
            skills = if (skills.isNotEmpty()) skills.joinToString(",") else null,
            query = query.ifEmpty { null },
            experience = if (minExperience > 0) minExperience.toString() else null,
            location = location.ifEmpty { null }
        )
    }

    // ── Get interviews for current user ───────────────────────────────────
    fun getInterviewsFlow(): Flow<List<InterviewResponse>> = flow {
        try {
            val interviews = RetrofitClient.api.getUpcomingInterviews()
            emit(interviews)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // ── Generate AI Cover Letter ──────────────────────────────────────────
    suspend fun generateCoverLetter(jobId: String): Result<String> = runCatching {
        val result = RetrofitClient.api.generateCoverLetter(jobId)
        result["cover_letter"] ?: throw Exception("No cover letter generated")
    }

    // ── Get Candidate Stats ───────────────────────────────────────────────
    fun getStatsFlow(): Flow<DashboardStats> = flow {
        try {
            val stats = RetrofitClient.api.getDashboardStats()
            emit(stats)
        } catch (e: Exception) {
            emit(DashboardStats())
        }
    }

    // ── Update Application Status ─────────────────────────────────────────
    suspend fun updateApplicationStatus(appId: String, status: String): Result<Unit> = runCatching {
        val response = RetrofitClient.api.updateApplicationStatus(appId, mapOf("status" to status))
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to update application status")
        }
    }

    // ── Add Candidate to Pipeline ─────────────────────────────────────────
    suspend fun addToPipeline(jobId: String, candidateUid: String): Result<String> = runCatching {
        val request = JobApplicationRequest(job = jobId.toInt(), student = candidateUid.toIntOrNull())
        val response = RetrofitClient.api.submitApplication(request)
        response.id
    }

    // ── Get Recruiter Stats ───────────────────────────────────────────────
    fun getRecruiterStatsFlow(): Flow<DashboardStats> = flow {
        try {
            val stats = RetrofitClient.api.getDashboardStats()
            emit(stats)
        } catch (e: Exception) {
            emit(DashboardStats())
        }
    }
}
