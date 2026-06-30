package com.fixmystreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId val id: String = "",
    val issueId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
