package com.fixmystreet.data.repository

import com.fixmystreet.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getNotifications(userId: String): List<Notification> {
        val snap = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        return snap.toObjects(Notification::class.java)
    }

    suspend fun markRead(notifId: String) {
        db.collection("notifications").document(notifId).update("read", true).await()
    }

    suspend fun markAllRead(userId: String) {
        val snap = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get().await()
            
        db.runBatch { batch ->
            for (doc in snap.documents) {
                batch.update(doc.reference, "read", true)
            }
        }.await()
    }

    suspend fun deleteNotification(notifId: String) {
        db.collection("notifications").document(notifId).delete().await()
    }
}
