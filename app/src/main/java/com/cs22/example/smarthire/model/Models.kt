package com.cs22.example.smarthire.model

// ═══════════════════════════════════════════════════
// JOB MODELS
// ═══════════════════════════════════════════════════

data class DjangoJob(
    val id: String? = null,
    val title: String = "",
    val company: String = "Internal",
    val description: String = "",
    val required_skills_json: List<String> = emptyList(),
    val skills_required: String = "",
    val min_experience: Int = 0,
    val experience_level: Int = 0,
    val degree_requirement: String = "",
    val job_type: String = "onsite",
    val location: String = "",
    val salary_range: String = "",
    val status: String = "active",
    val match_percentage: Double = 0.0,
    val applicant_count: Int = 0,
    val ai_screened_count: Int = 0,
    val created_at: String? = null,
    val recruiter_company: String? = null
) {
    val skillsList: List<String>
        get() = if (required_skills_json.isNotEmpty()) required_skills_json
                else skills_required.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

// ═══════════════════════════════════════════════════
// CV / PROFILE MODELS
// ═══════════════════════════════════════════════════

data class Education(
    val institution: String = "",
    val degree: String = "",
    val field: String = "",
    val year: String = ""
)

data class WorkExperience(
    val company: String = "",
    val role: String = "",
    val duration: String = "",
    val description: String = ""
)

data class ExtractedData(
    val skills: List<String> = emptyList(),
    val experience_years: Int = 0,
    val degree: String = "",
    val bio: String = "",
    val education: List<Education> = emptyList(),
    val work_experience: List<WorkExperience> = emptyList(),
    val certifications: List<String> = emptyList(),
    val location: String = "",
    val is_authentic: Boolean = true,
    val auth_message: String = "",
    val profile_completeness: Int = 0
)

data class ExtractedProfile(
    val cv: Any? = null,
    val extracted: ExtractedData? = null
)

data class CandidateProfileResponse(
    val id: String = "",
    val university: String? = null,
    val degree: String? = null,
    val degree_extracted: String? = null,
    val expected_graduation: String? = null,
    val skills: String? = null,
    val extracted_skills_json: List<String>? = null,
    val experience_years: Int = 0,
    val total_experience: Int = 0,
    val cv_file_path: String? = null,
    val user: UserInfo? = null,
    val match_percentage: Double = 0.0,
    val bio: String? = null,
    val location: String? = null,
    val github_url: String? = null,
    val linkedin_url: String? = null,
    val portfolio_url: String? = null,
    val education_json: List<Education>? = null,
    val work_experience_json: List<WorkExperience>? = null,
    val certifications_json: List<String>? = null,
    val profile_completeness: Int = 0,
    val profile_views_count: Int = 0,
    val is_searchable: Boolean = true
)

data class UserInfo(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val full_name: String = ""
)

// ═══════════════════════════════════════════════════
// AUTH MODELS
// ═══════════════════════════════════════════════════

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role_type: String,
    val first_name: String = "",
    val last_name: String = ""
)

data class GoogleLoginRequest(
    val id_token: String,
    val role_type: String
)

data class AuthResponse(
    val refresh: String,
    val access: String,
    val role: String? = null,
    val setup_complete: Boolean = false
)

data class ProfileResponse(
    val id: String = "",
    val username: String? = null,
    val email: String? = null,
    val role_type: String? = null,
    val full_name: String? = null,
    val profile: CandidateProfileResponse? = null,
    val university: String? = null,
    val degree: String? = null,
    val expected_graduation: String? = null,
    val skills: String? = null,
    val company_name: String? = null,
    val company_size: String? = null,
    val industry: String? = null
)

// ═══════════════════════════════════════════════════
// APPLICATION / ATS MODELS
// ═══════════════════════════════════════════════════

data class JobApplicationResponse(
    val id: String = "",
    val job: String = "",
    val status: String = "",
    val ats_status: String = "new",
    val match_score: Double = 0.0,
    val ai_match_score: Double = 0.0,
    val skill_gap_analysis: List<String> = emptyList(),
    val student_details: CandidateProfileResponse? = null,
    val candidate_details: CandidateProfileResponse? = null,
    val job_details: DjangoJob? = null,
    val applied_at: String? = null
) {
    val effectiveStatus: String get() = ats_status.ifEmpty { status }
    val effectiveMatchScore: Double get() = if (ai_match_score > 0) ai_match_score else match_score
    val effectiveCandidate: CandidateProfileResponse? get() = candidate_details ?: student_details
}

typealias Application = JobApplicationResponse

data class JobApplicationRequest(val job: Int, val student: Int? = null)

// ═══════════════════════════════════════════════════
// INTERVIEW MODELS
// ═══════════════════════════════════════════════════

data class InterviewResponse(
    val id: String = "",
    val application: String = "",
    val scheduled_at: String = "",
    val interview_type: String = "video",
    val meeting_link: String = "",
    val notes: String = "",
    val status: String = "scheduled",
    val duration_minutes: Int = 60,
    val job_title: String? = null,
    val company: String? = null,
    val candidate_name: String? = null
)

data class InterviewRequest(
    val application: Int,
    val scheduled_at: String,
    val interview_type: String = "video",
    val meeting_link: String = "",
    val notes: String = "",
    val duration_minutes: Int = 60
)

// ═══════════════════════════════════════════════════
// DASHBOARD STATS
// ═══════════════════════════════════════════════════

data class DashboardStats(
    val apps_sent: Int = 0,
    val profile_views: Int = 0,
    val profile_completeness: Int = 0,
    val active_jobs_count: Int = 0,
    val total_jobs: Int = 0,
    val active_jobs: Int = 0,
    val total_applications: Int = 0,
    val screened_candidates: Int = 0
)
