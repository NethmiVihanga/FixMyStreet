package com.fixmystreet.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    val currentUser get() = repo.currentUser

    fun register(name: String, email: String, phone: String, pass: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _authState.value = AuthState.Error("Please fill out all fields.")
            return
        }

        // Validate strict password rules:
        val hasMinLength = pass.length >= 8
        val hasUppercase = pass.any { it.isUpperCase() }
        val hasLowercase = pass.any { it.isLowerCase() }
        val hasDigit = pass.any { it.isDigit() }
        val hasSpecial = pass.any { !it.isLetterOrDigit() }

        if (!hasMinLength || !hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            _authState.value = AuthState.Error(
                "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one number, and one special character."
            )
            return
        }

        if (pass != confirmPass) {
            _authState.value = AuthState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.register(name, email, phone, pass)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.login(email, pass)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        repo.logout()
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.signInWithSocialProvider(activity, "google.com")
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google login failed")
            }
        }
    }

    fun signInWithFacebook(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.signInWithSocialProvider(activity, "facebook.com")
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Facebook login failed")
            }
        }
    }

    fun signInWithX(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.signInWithSocialProvider(activity, "twitter.com")
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "X login failed")
            }
        }
    }

    fun signInWithSocialGracefully(email: String, name: String, provider: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repo.signInWithSocialGracefully(email, name, provider)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "$provider login failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
