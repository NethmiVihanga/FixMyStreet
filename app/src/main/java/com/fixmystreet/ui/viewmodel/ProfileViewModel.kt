package com.fixmystreet.ui.viewmodel

import android.net.Uri
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.model.Issue
import com.fixmystreet.data.model.User
import com.fixmystreet.data.repository.IssueRepository
import com.fixmystreet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val profileRepo = ProfileRepository()
    private val issueRepo = IssueRepository()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _userIssues = MutableStateFlow<List<Issue>>(emptyList())
    val userIssues: StateFlow<List<Issue>> = _userIssues
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userProfile.value = profileRepo.getProfile(userId)
                _userIssues.value = issueRepo.getUserIssues(userId)
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(context: Context, userId: String, name: String, bio: String, phoneNumber: String, avatarUri: Uri?, deletePhoto: Boolean = false, onSuccess: () -> Unit) {
         viewModelScope.launch {
              _isLoading.value = true
              _error.value = null
              try {
                  profileRepo.updateProfile(context, userId, name, bio, phoneNumber, avatarUri, deletePhoto)
                  loadProfile(userId)
                  onSuccess()
              } catch (e: Exception) {
                  _error.value = e.message ?: "Unknown error occurred"
              } finally {
                  _isLoading.value = false
              }
         }
    }

    fun deactivateAccount(userId: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
             _isLoading.value = true
             try {
                 profileRepo.deactivateAccount(userId)
                 onSuccess()
             } catch (e: Exception) {
                 // handle error
             } finally {
                 _isLoading.value = false
             }
         }
    }
}
