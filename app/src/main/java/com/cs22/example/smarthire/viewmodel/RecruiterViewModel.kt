package com.cs22.example.smarthire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs22.example.smarthire.firebase.JobRepository
import com.cs22.example.smarthire.firebase.CandidateRepository
import com.cs22.example.smarthire.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class RecruiterUiState<out T> {
    object Idle : RecruiterUiState<Nothing>()
    object Loading : RecruiterUiState<Nothing>()
    data class Success<T>(val data: T) : RecruiterUiState<T>()
    data class Error(val message: String) : RecruiterUiState<Nothing>()
}

class RecruiterViewModel : ViewModel() {

    private val _myPostingsState = MutableStateFlow<RecruiterUiState<List<DjangoJob>>>(RecruiterUiState.Idle)
    val myPostingsState = _myPostingsState.asStateFlow()

    private val _statsState = MutableStateFlow<RecruiterUiState<DashboardStats>>(RecruiterUiState.Idle)
    val statsState = _statsState.asStateFlow()

    private val _postJobState = MutableStateFlow<RecruiterUiState<Unit>>(RecruiterUiState.Idle)
    val postJobState = _postJobState.asStateFlow()

    private val _candidatesState = MutableStateFlow<RecruiterUiState<List<CandidateProfileResponse>>>(RecruiterUiState.Idle)
    val candidatesState = _candidatesState.asStateFlow()

    private val _applicationsState = MutableStateFlow<RecruiterUiState<List<Application>>>(RecruiterUiState.Idle)
    val applicationsState = _applicationsState.asStateFlow()

    init {
        observeMyPostings()
        observeStats()
        observeApplications()
    }

    private fun observeMyPostings() {
        viewModelScope.launch {
            _myPostingsState.value = RecruiterUiState.Loading
            JobRepository.getMyPostingsFlow().collect { jobs ->
                _myPostingsState.value = RecruiterUiState.Success(jobs)
            }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            _statsState.value = RecruiterUiState.Loading
            JobRepository.getRecruiterStatsFlow().collect { stats ->
                _statsState.value = RecruiterUiState.Success(stats)
            }
        }
    }

    private fun observeApplications() {
        viewModelScope.launch {
            _applicationsState.value = RecruiterUiState.Loading
            JobRepository.getMyApplicationsFlow().collect { apps ->
                _applicationsState.value = RecruiterUiState.Success(apps)
            }
        }
    }

    fun postJob(
        title: String, company: String, description: String, 
        skills: List<String>, minExp: Int, degree: String, 
        type: String, location: String, salary: String
    ) {
        viewModelScope.launch {
            _postJobState.value = RecruiterUiState.Loading
            JobRepository.postJob(
                title, company, location, type, "General", description, salary, skills, minExp.toString()
            ).onSuccess {
                _postJobState.value = RecruiterUiState.Success(Unit)
            }.onFailure { e ->
                _postJobState.value = RecruiterUiState.Error(e.message ?: "Post failed")
            }
        }
    }

    fun resetPostJobState() {
        _postJobState.value = RecruiterUiState.Idle
    }

    fun toggleJobStatus(jobId: String) {
        viewModelScope.launch {
            // In a real app, we would toggle status based on current state
            JobRepository.toggleJobStatus(jobId, "active") 
        }
    }

    fun searchCandidates(skills: String, minExperience: String, degree: String) {
        viewModelScope.launch {
            _candidatesState.value = RecruiterUiState.Loading
            val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val minExpInt = minExperience.toIntOrNull() ?: 0
            
            CandidateRepository.searchCandidatesWithAlgorithm(skillsList, minExpInt, degree)
                .onSuccess { candidates ->
                    _candidatesState.value = RecruiterUiState.Success(candidates)
                }
                .onFailure { e ->
                    _candidatesState.value = RecruiterUiState.Error(e.message ?: "Search failed")
                }
        }
    }

    fun fetchApplications() {
        // Handled by observeApplications()
    }

    fun updateApplicationStatus(appId: String, status: String) {
        viewModelScope.launch {
            JobRepository.updateApplicationStatus(appId, status)
        }
    }

    fun addToPipeline(candidateUid: String) {
        viewModelScope.launch {
            // Default to the first active job for now, or would normally show a picker
            val firstJob = (myPostingsState.value as? RecruiterUiState.Success)?.data?.firstOrNull()
            firstJob?.id?.let { jobId ->
                JobRepository.addToPipeline(jobId.toString(), candidateUid)
            }
        }
    }
}
