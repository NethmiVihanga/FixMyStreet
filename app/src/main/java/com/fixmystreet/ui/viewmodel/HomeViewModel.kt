package com.fixmystreet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.model.Issue
import com.fixmystreet.data.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repo = IssueRepository()

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadIssues(filterStatus: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                var fetchedIssues = repo.getIssues(filterStatus)
                if (fetchedIssues.isEmpty() && filterStatus == null) {
                    repo.seedMockIssues()
                    fetchedIssues = repo.getIssues(filterStatus)
                }
                _issues.value = fetchedIssues
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load issues"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
