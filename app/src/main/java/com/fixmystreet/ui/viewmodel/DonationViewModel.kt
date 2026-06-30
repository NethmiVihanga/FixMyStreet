package com.fixmystreet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmystreet.data.model.Donation
import com.fixmystreet.data.repository.DonationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DonationViewModel : ViewModel() {
    private val repo = DonationRepository()

    private val _donations = MutableStateFlow<List<Donation>>(emptyList())
    val donations: StateFlow<List<Donation>> = _donations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadDonations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _donations.value = repo.getDonations(userId)
            } catch (e: Exception) {
                // handle
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitDonation(userId: String, userName: String, amount: Double, message: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
             _isLoading.value = true
             try {
                 repo.submitDonation(userId, userName, amount, message)
                 loadDonations(userId)
                 onSuccess()
             } catch (e: Exception) {
                 // handle
             } finally {
                 _isLoading.value = false
             }
         }
    }

    fun cancelDonation(donationId: String, userId: String) {
         viewModelScope.launch {
             try {
                 repo.cancelDonation(donationId)
                 loadDonations(userId)
             } catch (e: Exception) {
                 // handle
             }
         }
    }

    fun deleteDonation(donationId: String, userId: String) {
         viewModelScope.launch {
             try {
                 repo.deleteDonation(donationId)
                 loadDonations(userId)
             } catch (e: Exception) {
                 // handle
             }
         }
    }
}
