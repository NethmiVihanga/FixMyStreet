package com.fixmystreet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.model.Notification
import com.fixmystreet.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repo = NotificationRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _notifications.value = repo.getNotifications(userId)
            } catch (e: Exception) {
                // handle
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markRead(notifId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.markRead(notifId)
                loadNotifications(userId)
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun markAllRead(userId: String) {
         viewModelScope.launch {
            try {
                repo.markAllRead(userId)
                loadNotifications(userId)
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun deleteNotification(notifId: String, userId: String) {
         viewModelScope.launch {
            try {
                repo.deleteNotification(notifId)
                loadNotifications(userId)
            } catch (e: Exception) {
                // handle
            }
        }
    }
}
