package com.cs22.example.smarthire.network

import com.cs22.example.smarthire.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface SmartHireApi {

    // ═══════════════════════════════════════════════════
    // AUTH
    // ═══════════════════════════════════════════════════
    
    @POST("api/users/register/")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/users/google-login/")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthResponse

    @POST("api/users/login/")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // ═══════════════════════════════════════════════════
    // JOBS — Search, Post, Toggle, Stats
    // ═══════════════════════════════════════════════════
    
    @GET("api/jobs/jobs/")
    suspend fun getJobs(
        @Query("skills") skills: String? = null,
        @Query("q") query: String? = null,
        @Query("experience") experience: String? = null,
        @Query("type") type: String? = null,
        @Query("location") location: String? = null
    ): List<DjangoJob>

    @GET("api/jobs/jobs/top_match/")
    suspend fun getTopMatch(): DjangoJob

    @GET("api/jobs/jobs/my_postings/")
    suspend fun getMyPostings(): List<DjangoJob>

    @GET("api/jobs/jobs/stats/")
    suspend fun getDashboardStats(): DashboardStats

    @GET("api/jobs/jobs/{id}/generate_cover_letter/")
    suspend fun generateCoverLetter(@Path("id") id: String): Map<String, String>

    @PATCH("api/jobs/jobs/{id}/toggle_status/")
    suspend fun toggleJobStatus(@Path("id") id: String): DjangoJob

    @POST("api/jobs/jobs/")
    suspend fun postJob(@Body job: Map<String, @JvmSuppressWildcards Any>): Response<DjangoJob>

    // ═══════════════════════════════════════════════════
    // CANDIDATES — Search & View
    // ═══════════════════════════════════════════════════
    
    @GET("api/users/search/candidates/")
    suspend fun searchCandidates(
        @Query("skills") skills: String? = null,
        @Query("degree") degree: String? = null,
        @Query("experience") experience: String? = null
    ): List<CandidateProfileResponse>

    @GET("api/users/candidates/{id}/")
    suspend fun getCandidateProfile(@Path("id") id: String): CandidateProfileResponse

    // ═══════════════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════════════
    
    @GET("api/users/profile/")
    suspend fun getProfile(): ProfileResponse

    @PATCH("api/users/profile/")
    suspend fun updateProfile(@Body payload: Map<String, @JvmSuppressWildcards Any>): CandidateProfileResponse

    @PUT("api/users/profile/setup/")
    suspend fun setupProfile(@Body payload: Map<String, String>): Response<Unit>

    // ═══════════════════════════════════════════════════
    // APPLICATIONS / ATS
    // ═══════════════════════════════════════════════════
    
    @GET("api/jobs/applications/")
    suspend fun getApplications(): List<JobApplicationResponse>

    @PATCH("api/jobs/applications/{id}/")
    suspend fun updateApplicationStatus(
        @Path("id") id: String,
        @Body statusData: Map<String, String>
    ): Response<JobApplicationResponse>

    @POST("api/jobs/applications/")
    suspend fun submitApplication(@Body request: JobApplicationRequest): JobApplicationResponse

    // ═══════════════════════════════════════════════════
    // CV UPLOAD
    // ═══════════════════════════════════════════════════
    
    @Multipart
    @POST("api/cv_bank/cvs/")
    suspend fun uploadCV(@Part cvFile: MultipartBody.Part): ExtractedProfile

    // ═══════════════════════════════════════════════════
    // INTERVIEWS
    // ═══════════════════════════════════════════════════
    
    @GET("api/interviews/upcoming/")
    suspend fun getUpcomingInterviews(): List<InterviewResponse>

    @GET("api/interviews/")
    suspend fun getInterviews(): List<InterviewResponse>

    @POST("api/interviews/")
    suspend fun scheduleInterview(@Body request: InterviewRequest): InterviewResponse

    // ═══════════════════════════════════════════════════
    // COMMUNICATIONS
    // ═══════════════════════════════════════════════════
    
    @GET("api/communications/comm/application_chat/")
    suspend fun getMessages(@Query("application_id") applicationId: String): ChatThreadResponse

    @POST("api/communications/comm/send_message/")
    suspend fun sendMessage(@Body req: SendMessageRequest): ChatMessage

    @GET("api/communications/comm/notifications/")
    suspend fun getNotifications(): List<Notification>

    // ═══════════════════════════════════════════════════
    // AI ENGINE — Skill Gap, Recommendations, Match Score
    // ═══════════════════════════════════════════════════

    @GET("api/ai/skill-gap/{applicationId}/")
    suspend fun getSkillGap(@Path("applicationId") applicationId: Int): Map<String, Any>

    @GET("api/ai/recommended-jobs/")
    suspend fun getRecommendedJobs(): List<DjangoJob>

    @GET("api/ai/match-score/{jobId}/")
    suspend fun getMatchScore(@Path("jobId") jobId: Int): Map<String, Any>

    // ═══════════════════════════════════════════════════
    // FCM — Push Notification Token
    // ═══════════════════════════════════════════════════

    @POST("api/users/fcm-token/")
    suspend fun registerFcmToken(@Body body: Map<String, String>): Map<String, Any>

    // ═══════════════════════════════════════════════════
    // INTERVIEW ACTIONS
    // ═══════════════════════════════════════════════════

    @PATCH("api/interviews/{id}/cancel/")
    suspend fun cancelInterview(@Path("id") id: Int): InterviewResponse

    @PATCH("api/interviews/{id}/complete/")
    suspend fun completeInterview(
        @Path("id") id: Int,
        @Body body: Map<String, String> = emptyMap()
    ): InterviewResponse
}

object RetrofitClient {
    // ═══════════════════════════════════════════════════
    // CLOUD BASE URL — Update this after deployment
    // ═══════════════════════════════════════════════════
    // For local development: "http://192.168.1.40:8000/"
    // For Railway cloud: "https://your-app.up.railway.app/"
    private const val BASE_URL = "https://Yaqoob9227.pythonanywhere.com/"
    
    lateinit var tokenManager: TokenManager

    fun init(context: android.content.Context) {
        tokenManager = TokenManager(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        if (this::tokenManager.isInitialized) {
            tokenManager.getAccessToken()?.let {
                request.addHeader("Authorization", "Bearer $it")
            }
        }
        chain.proceed(request.build())
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val api: SmartHireApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartHireApi::class.java)
    }
}
