package com.fixmystreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoURL: String = "",
    val phone: String = "",
    val bio: String = "",
    val issuesCount: Int = 0,
    val resolvedCount: Int = 0,
    val deactivated: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
