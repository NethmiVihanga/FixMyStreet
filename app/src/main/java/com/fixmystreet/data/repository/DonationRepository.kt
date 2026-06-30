package com.fixmystreet.data.repository

import com.fixmystreet.data.model.Donation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class DonationRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun submitDonation(userId: String, userName: String, amount: Double, message: String): String {
        val donation = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "amount" to amount,
            "message" to message,
            "status" to "completed",
            "createdAt" to FieldValue.serverTimestamp()
        )
        val ref = db.collection("donations").add(donation).await()
        
        try {
            val notif = hashMapOf(
                "userId" to userId,
                "title" to "💖 Donation Successful",
                "message" to "Thank you for your donation of $${String.format(java.util.Locale.US, "%.2f", amount)}!",
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("notifications").add(notif).await()
        } catch (e: Exception) {
            // ignore
        }

        return ref.id
    }

    suspend fun getDonations(userId: String): List<Donation> {
        val snap = db.collection("donations")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        return snap.toObjects(Donation::class.java)
    }

    suspend fun cancelDonation(donationId: String) {
        db.collection("donations").document(donationId).update("status", "cancelled").await()
    }

    suspend fun deleteDonation(donationId: String) {
        db.collection("donations").document(donationId).delete().await()
    }
}
