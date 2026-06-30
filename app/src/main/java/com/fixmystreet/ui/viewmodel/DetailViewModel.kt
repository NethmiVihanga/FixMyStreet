package com.fixmystreet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.model.Comment
import com.fixmystreet.data.model.Issue
import com.fixmystreet.data.repository.CommentRepository
import com.fixmystreet.data.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val issueRepo = IssueRepository()
    private val commentRepo = CommentRepository()

    private val _issue = MutableStateFlow<Issue?>(null)
    val issue: StateFlow<Issue?> = _issue

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadIssueDetails(issueId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _issue.value = issueRepo.getIssue(issueId)
                _comments.value = commentRepo.getComments(issueId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(issueId: String, status: String) {
        viewModelScope.launch {
            try {
                issueRepo.updateIssue(issueId, mapOf("status" to status))
                loadIssueDetails(issueId) // reload
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateIssueDetails(issueId: String, title: String, desc: String, loc: String) {
         viewModelScope.launch {
             try {
                 issueRepo.updateIssue(issueId, mapOf(
                     "title" to title,
                     "description" to desc,
                     "location" to loc
                 ))
                 loadIssueDetails(issueId)
             } catch (e: Exception) {
                 _error.value = e.message
             }
         }
    }

    fun deleteIssue(issueId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
             try {
                 issueRepo.deleteIssue(issueId)
                 onSuccess()
             } catch (e: Exception) {
                 _error.value = e.message
             }
        }
    }

    fun addComment(issueId: String, userId: String, userName: String, text: String) {
        viewModelScope.launch {
            try {
                commentRepo.addComment(issueId, userId, userName, text)
                _comments.value = commentRepo.getComments(issueId) // reload
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun editComment(issueId: String, commentId: String, text: String) {
         viewModelScope.launch {
            try {
                commentRepo.updateComment(issueId, commentId, text)
                _comments.value = commentRepo.getComments(issueId)
            } catch (e: Exception) {
                _error.value = e.message
            }
         }
    }

    fun deleteComment(issueId: String, commentId: String) {
         viewModelScope.launch {
            try {
                commentRepo.deleteComment(issueId, commentId)
                _comments.value = commentRepo.getComments(issueId)
            } catch (e: Exception) {
                _error.value = e.message
            }
         }
    }
}
