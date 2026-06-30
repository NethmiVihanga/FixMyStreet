package com.fixmystreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Issue(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val photoURL: String = "",
    val status: String = "pending", // pending, in-progress, resolved, rejected
    val commentsCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
