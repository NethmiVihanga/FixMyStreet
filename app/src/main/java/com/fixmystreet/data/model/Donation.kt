package com.fixmystreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Donation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val message: String = "",
    val status: String = "completed", // completed, cancelled
    val createdAt: Timestamp? = null
)
