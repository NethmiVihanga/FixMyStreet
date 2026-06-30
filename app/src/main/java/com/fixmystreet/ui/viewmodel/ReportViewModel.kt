package com.fixmystreet.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = IssueRepository()

    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state: StateFlow<ReportState> = _state

    fun submitReport(
        userId: String,
        userName: String,
        title: String,
        description: String,
        category: String,
        location: String,
        lat: Double?,
        lng: Double?,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            _state.value = ReportState.Loading
            try {
                repo.createIssue(
                    context = getApplication(),
                    userId = userId,
                    userName = userName,
                    title = title,
                    description = description,
                    category = category,
                    location = location,
                    lat = lat,
                    lng = lng,
                    photoUri = photoUri
                )
                _state.value = ReportState.Success
            } catch (e: Exception) {
                _state.value = ReportState.Error(e.message ?: "Failed to submit report")
            }
        }
    }

    fun resetState() {
        _state.value = ReportState.Idle
    }
}

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    object Success : ReportState()
    data class Error(val message: String) : ReportState()
}
