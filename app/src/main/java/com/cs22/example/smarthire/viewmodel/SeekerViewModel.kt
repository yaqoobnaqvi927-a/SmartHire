package com.cs22.example.smarthire.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs22.example.smarthire.firebase.CvRepository
import com.cs22.example.smarthire.firebase.JobRepository
import com.cs22.example.smarthire.firebase.FirebaseClient
import com.cs22.example.smarthire.model.*
import com.cs22.example.smarthire.network.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SeekerUiState<out T> {
    object Idle : SeekerUiState<Nothing>()
    object Loading : SeekerUiState<Nothing>()
    data class Success<T>(val data: T) : SeekerUiState<T>()
    data class Error(val message: String) : SeekerUiState<Nothing>()
}

class SeekerViewModel : ViewModel() {

    private val _topMatchState = MutableStateFlow<SeekerUiState<DjangoJob>>(SeekerUiState.Idle)
    val topMatchState = _topMatchState.asStateFlow()

    private val _jobsState = MutableStateFlow<SeekerUiState<List<DjangoJob>>>(SeekerUiState.Idle)
    val jobsState = _jobsState.asStateFlow()

    private val _applicationsState = MutableStateFlow<SeekerUiState<List<Application>>>(SeekerUiState.Idle)
    val applicationsState = _applicationsState.asStateFlow()

    private val _profileState = MutableStateFlow<SeekerUiState<ProfileResponse>>(SeekerUiState.Idle)
    val profileState = _profileState.asStateFlow()

    private val _cvSyncState = MutableStateFlow<SeekerUiState<Unit>>(SeekerUiState.Idle)
    val cvSyncState = _cvSyncState.asStateFlow()

    private val _coverLetterState = MutableStateFlow<SeekerUiState<String>>(SeekerUiState.Idle)
    val coverLetterState = _coverLetterState.asStateFlow()

    private val _statsState = MutableStateFlow<SeekerUiState<DashboardStats>>(SeekerUiState.Idle)
    val statsState = _statsState.asStateFlow()

    private val _interviewsState = MutableStateFlow<SeekerUiState<List<InterviewResponse>>>(SeekerUiState.Idle)
    val interviewsState = _interviewsState.asStateFlow()

    // AI Engine features States
    private val _skillGapState = MutableStateFlow<SeekerUiState<Map<String, Any>>>(SeekerUiState.Idle)
    val skillGapState = _skillGapState.asStateFlow()

    private val _recommendedJobsState = MutableStateFlow<SeekerUiState<List<DjangoJob>>>(SeekerUiState.Idle)
    val recommendedJobsState = _recommendedJobsState.asStateFlow()

    private val _matchScoreState = MutableStateFlow<SeekerUiState<Map<String, Any>>>(SeekerUiState.Idle)
    val matchScoreState = _matchScoreState.asStateFlow()

    init {
        observeJobs()
        observeApplications()
        observeStats()
        observeInterviews()
        getProfile()
        fetchRecommendedJobs()
    }

    fun observeJobs() {
        viewModelScope.launch {
            _jobsState.value = SeekerUiState.Loading
            JobRepository.getJobsFlow().collect { jobs ->
                _jobsState.value = SeekerUiState.Success(jobs)
                val top = jobs.maxByOrNull { it.match_percentage }
                if (top != null && top.match_percentage > 0) {
                    _topMatchState.value = SeekerUiState.Success(top)
                }
            }
        }
    }

    fun observeApplications() {
        viewModelScope.launch {
            _applicationsState.value = SeekerUiState.Loading
            JobRepository.getMyApplicationsFlow().collect { apps ->
                _applicationsState.value = SeekerUiState.Success(apps)
            }
        }
    }

    fun observeStats() {
        viewModelScope.launch {
            _statsState.value = SeekerUiState.Loading
            JobRepository.getStatsFlow().collect { stats ->
                _statsState.value = SeekerUiState.Success(stats)
            }
        }
    }

    fun observeInterviews() {
        viewModelScope.launch {
            _interviewsState.value = SeekerUiState.Loading
            JobRepository.getInterviewsFlow().collect { interviews ->
                _interviewsState.value = SeekerUiState.Success(interviews)
            }
        }
    }

    fun searchJobs(skills: String, minExperience: String, location: String, degree: String) {
        viewModelScope.launch {
            _jobsState.value = SeekerUiState.Loading
            val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val minExpInt = minExperience.toIntOrNull() ?: 0
            
            JobRepository.searchJobsWithAlgorithm(skills, skillsList, minExpInt, location)
                .onSuccess { results ->
                    _jobsState.value = SeekerUiState.Success(results)
                }
                .onFailure { e ->
                    _jobsState.value = SeekerUiState.Error(e.message ?: "Search failed")
                }
        }
    }

    fun uploadCv(fileUri: Uri, fileName: String) {
        viewModelScope.launch {
            _cvSyncState.value = SeekerUiState.Loading
            CvRepository.uploadCv(fileUri, fileName)
                .onSuccess { 
                    _cvSyncState.value = SeekerUiState.Success(Unit)
                    getProfile() // Refresh profile after parsing
                    fetchRecommendedJobs() // Refresh recommended jobs based on new skills
                }
                .onFailure { e ->
                    _cvSyncState.value = SeekerUiState.Error(e.message ?: "Upload failed")
                }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _profileState.value = SeekerUiState.Loading
            try {
                val profile = RetrofitClient.api.getProfile()
                _profileState.value = SeekerUiState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = SeekerUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    // AI Engine calls
    fun fetchSkillGap(applicationId: Int) {
        viewModelScope.launch {
            _skillGapState.value = SeekerUiState.Loading
            try {
                val result = RetrofitClient.api.getSkillGap(applicationId)
                _skillGapState.value = SeekerUiState.Success(result)
            } catch (e: Exception) {
                _skillGapState.value = SeekerUiState.Error(e.message ?: "Failed to load skill gap analysis")
            }
        }
    }

    fun fetchRecommendedJobs() {
        viewModelScope.launch {
            _recommendedJobsState.value = SeekerUiState.Loading
            try {
                val jobs = RetrofitClient.api.getRecommendedJobs()
                _recommendedJobsState.value = SeekerUiState.Success(jobs)
            } catch (e: Exception) {
                _recommendedJobsState.value = SeekerUiState.Error(e.message ?: "Failed to load recommended jobs")
            }
        }
    }

    fun fetchMatchScore(jobId: Int) {
        viewModelScope.launch {
            _matchScoreState.value = SeekerUiState.Loading
            try {
                val result = RetrofitClient.api.getMatchScore(jobId)
                _matchScoreState.value = SeekerUiState.Success(result)
            } catch (e: Exception) {
                _matchScoreState.value = SeekerUiState.Error(e.message ?: "Failed to load match score")
            }
        }
    }

    // Interview Actions
    fun cancelInterview(interviewId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.cancelInterview(interviewId)
                observeInterviews() // Refresh the interviews list
            } catch (e: Exception) {
                // Fail silently or handle if necessary
            }
        }
    }

    fun completeInterview(interviewId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.completeInterview(interviewId)
                observeInterviews() // Refresh the interviews list
            } catch (e: Exception) {
                // Fail silently or handle if necessary
            }
        }
    }

    fun getApplications() {
        observeApplications()
    }

    fun applyForJob(jobId: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            JobRepository.applyToJob(jobId)
                .onSuccess { appId -> 
                    onComplete(appId)
                    observeApplications() // Refresh applications
                }
        }
    }

    fun generateCoverLetter(jobId: String) {
        viewModelScope.launch {
            _coverLetterState.value = SeekerUiState.Loading
            JobRepository.generateCoverLetter(jobId)
                .onSuccess { text -> _coverLetterState.value = SeekerUiState.Success(text) }
                .onFailure { e -> _coverLetterState.value = SeekerUiState.Error(e.message ?: "Failed to generate cover letter") }
        }
    }

    fun resetCoverLetterState() {
        _coverLetterState.value = SeekerUiState.Idle
    }

    fun logout(onComplete: () -> Unit) {
        FirebaseClient.auth.signOut()
        RetrofitClient.tokenManager.clear()
        onComplete()
    }
}
