package com.fixmystreet.data.repository

import com.fixmystreet.data.model.Comment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CommentRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun commentsRef(issueId: String) = 
        db.collection("issues").document(issueId).collection("comments")

    suspend fun addComment(issueId: String, userId: String, userName: String, text: String): String {
        val comment = hashMapOf(
            "issueId" to issueId,
            "userId" to userId,
            "userName" to userName,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val ref = commentsRef(issueId).add(comment).await()
        
        // Increment comments count on issue
        db.collection("issues").document(issueId)
            .update("commentsCount", FieldValue.increment(1)).await()
            
        return ref.id
    }

    suspend fun getComments(issueId: String): List<Comment> {
        val snap = commentsRef(issueId).orderBy("createdAt", Query.Direction.ASCENDING).get().await()
        return snap.toObjects(Comment::class.java)
    }

    suspend fun updateComment(issueId: String, commentId: String, text: String) {
        commentsRef(issueId).document(commentId).update(
            "text", text,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun deleteComment(issueId: String, commentId: String) {
        commentsRef(issueId).document(commentId).delete().await()
        db.collection("issues").document(issueId)
            .update("commentsCount", FieldValue.increment(-1)).await()
    }
}
