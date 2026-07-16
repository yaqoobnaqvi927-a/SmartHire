package com.cs22.example.smarthire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs22.example.smarthire.model.DjangoJob
import com.cs22.example.smarthire.model.ExtractedProfile
import com.cs22.example.smarthire.model.JobApplicationResponse
import com.cs22.example.smarthire.model.ProfileResponse
import com.cs22.example.smarthire.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.IOException

class JobSeekerViewModel : ViewModel() {

    private val _jobsState = MutableStateFlow<UiState<List<DjangoJob>>>(UiState.Initial)
    val jobsState: StateFlow<UiState<List<DjangoJob>>> = _jobsState.asStateFlow()

    private val _uploadState = MutableStateFlow<UiState<ExtractedProfile>>(UiState.Initial)
    val uploadState: StateFlow<UiState<ExtractedProfile>> = _uploadState.asStateFlow()

    private val _profileState = MutableStateFlow<UiState<ProfileResponse>>(UiState.Initial)
    val profileState: StateFlow<UiState<ProfileResponse>> = _profileState.asStateFlow()

    private val _applicationsState = MutableStateFlow<UiState<List<JobApplicationResponse>>>(UiState.Initial)
    val applicationsState: StateFlow<UiState<List<JobApplicationResponse>>> = _applicationsState.asStateFlow()

    init {
        fetchProfile()
        fetchApplications()
    }

    fun fetchJobs() {
        viewModelScope.launch {
            _jobsState.value = UiState.Loading
            try {
                // Execute network call bridging to Django E:/FYP backend
                val jobs = RetrofitClient.api.getJobs()
                _jobsState.value = UiState.Success(jobs)
            } catch (e: IOException) {
                // Map connection errors specifically since emulator uses 10.0.2.2
                _jobsState.value = UiState.Error("Network Error. Is Django running? ${e.localizedMessage}")
            } catch (e: Exception) {
                _jobsState.value = UiState.Error(e.message ?: "Unknown Error occurred")
            }
        }
    }

    fun uploadCVBytes(cvPart: MultipartBody.Part) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                // Post Multipart payload to Django NLP pipeline
                val result = RetrofitClient.api.uploadCV(cvPart)
                _uploadState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "CV Parsing Failed")
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                val profile = RetrofitClient.api.getProfile()
                _profileState.value = UiState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    private fun fetchApplications() {
        viewModelScope.launch {
            _applicationsState.value = UiState.Loading
            try {
                val apps = RetrofitClient.api.getApplications()
                _applicationsState.value = UiState.Success(apps)
            } catch (e: Exception) {
                _applicationsState.value = UiState.Error(e.message ?: "Failed to load apps")
            }
        }
    }
    
    // Helper to reset upload state back to initial when navigating away
    fun resetUploadState() {
        _uploadState.value = UiState.Initial
    }
}
