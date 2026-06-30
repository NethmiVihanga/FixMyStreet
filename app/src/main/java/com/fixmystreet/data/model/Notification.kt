package com.fixmystreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val issueId: String? = null,
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)
