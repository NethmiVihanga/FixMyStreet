package com.fixmystreet.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.fixmystreet.data.model.Issue
import com.fixmystreet.data.model.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class IssueRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createIssue(
        context: Context,
        userId: String,
        userName: String,
        title: String,
        description: String,
        category: String,
        location: String,
        lat: Double?,
        lng: Double?,
        photoUri: Uri?
    ): String {
        var photoUrl = ""
        if (photoUri != null) {
            val uriStr = photoUri.toString()
            when {
                uriStr.startsWith("http://") || uriStr.startsWith("https://") -> {
                    // Already a web URL — use directly, no upload needed
                    photoUrl = uriStr
                }
                else -> {
                    // Local file URI — compress with Bitmap API and store as Base64 in Firestore
                    // This avoids Firebase Storage (which requires billing upgrade)
                    val inputStream = context.contentResolver.openInputStream(photoUri)
                        ?: throw Exception("Cannot open image file. Please try selecting the photo again.")
                    val rawBytes = inputStream.use { it.readBytes() }
                    if (rawBytes.isEmpty()) throw Exception("Selected image appears to be empty.")

                    // Decode → scale down to max 800px → compress to JPEG 60%
                    val original = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                        ?: throw Exception("Could not decode image. Please choose a different photo.")
                    val scaled = scaleBitmap(original, maxDim = 800)
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 60, out)
                    val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    photoUrl = "b64:$base64"
                }
            }
        }

        val issueData = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "title" to title,
            "description" to description,
            "category" to category,
            "location" to location,
            "lat" to lat,
            "lng" to lng,
            "photoURL" to photoUrl,
            "status" to "pending",
            "commentsCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val docRef = db.collection("issues").add(issueData).await()
        
        // Broadcast notification to ALL users so everyone's Notifications page is populated
        try {
            val allUsers = db.collection("users").get().await()
            val batch = db.batch()
            for (userDoc in allUsers.documents) {
                val uid = userDoc.id
                val isReporter = uid == userId
                val notifData = hashMapOf(
                    "userId" to uid,
                    "title" to if (isReporter) "🛠️ Issue Reported" else "🆕 New Issue Reported",
                    "message" to if (isReporter)
                        "Your issue \"$title\" has been submitted successfully."
                    else
                        "A new issue \"$title\" was reported at $location.",
                    "issueId" to docRef.id,
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                val notifRef = db.collection("notifications").document()
                batch.set(notifRef, notifData)
            }
            batch.commit().await()
        } catch (e: Exception) {
            // ignore — notification failure should not block issue creation
        }
        
        return docRef.id
    }

    suspend fun getIssues(filterStatus: String? = null): List<Issue> {
        var query: Query = db.collection("issues")
        if (filterStatus != null) {
            query = query.whereEqualTo("status", filterStatus)
        }
        query = query.orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
        
        val snap = query.get().await()
        return snap.toObjects(Issue::class.java)
    }

    suspend fun getIssue(id: String): Issue? {
        val snap = db.collection("issues").document(id).get().await()
        return snap.toObject(Issue::class.java)
    }

    suspend fun getUserIssues(userId: String): List<Issue> {
        val snap = db.collection("issues")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        return snap.toObjects(Issue::class.java)
    }

    suspend fun updateIssue(id: String, updates: Map<String, Any>) {
        val map = updates.toMutableMap()
        map["updatedAt"] = FieldValue.serverTimestamp()
        db.collection("issues").document(id).update(map).await()
    }

    suspend fun deleteIssue(id: String) {
        db.collection("issues").document(id).delete().await()
    }

    suspend fun seedMockIssues() {
        val snap = db.collection("issues").get().await()
        
        if (snap.isEmpty) {
            val mockIssues = listOf(
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Broken Road",
                    "location" to "Borella",
                    "description" to "The road has sunk and cracked, causing a hazard.",
                    "photoURL" to "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?q=80&w=600&auto=format&fit=crop",
                    "category" to "Road",
                    "status" to "pending",
                    "lat" to 6.9200,
                    "lng" to 79.8794,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Broken Bridge",
                    "location" to "Kandy",
                    "description" to "Wooden planks are missing from the bridge.",
                    "photoURL" to "https://images.unsplash.com/photo-1473615694875-101168fbc9fb?q=80&w=600&auto=format&fit=crop",
                    "category" to "Infrastructure",
                    "status" to "pending",
                    "lat" to 7.2906,
                    "lng" to 80.6337,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Broken Traffic light",
                    "location" to "Galle",
                    "description" to "Traffic light is bent over and not working.",
                    "photoURL" to "https://images.unsplash.com/photo-1598463952796-088f11b2b8d0?q=80&w=600&auto=format&fit=crop",
                    "category" to "Traffic",
                    "status" to "pending",
                    "lat" to 6.0535,
                    "lng" to 80.2210,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Road in progress",
                    "location" to "Nugegoda",
                    "description" to "Road work has been abandoned for weeks.",
                    "photoURL" to "https://images.unsplash.com/photo-1584852959828-090c291bd670?q=80&w=600&auto=format&fit=crop",
                    "category" to "Road",
                    "status" to "in-progress",
                    "lat" to 6.8741,
                    "lng" to 79.8927,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Flyover Work",
                    "location" to "Bambalapitiya",
                    "description" to "Construction of the new pedestrian flyover.",
                    "photoURL" to "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?q=80&w=600&auto=format&fit=crop",
                    "category" to "Traffic",
                    "status" to "in-progress",
                    "lat" to 6.8970,
                    "lng" to 79.8550,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                hashMapOf(
                    "userId" to "mockUser",
                    "userName" to "Admin",
                    "title" to "Drainage Construction",
                    "location" to "Colombo Fort",
                    "description" to "Installing new storm drains along the main road.",
                    "photoURL" to "https://images.unsplash.com/photo-1504307651254-35680f356dfd?q=80&w=600&auto=format&fit=crop",
                    "category" to "Infrastructure",
                    "status" to "in-progress",
                    "lat" to 6.9310,
                    "lng" to 79.8510,
                    "commentsCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            for (issue in mockIssues) {
                db.collection("issues").add(issue).await()
            }
        } else {
            // Self-healing: Update existing coordinate-less issues and status
            for (doc in snap.documents) {
                val issue = doc.toObject(Issue::class.java)
                if (issue != null && issue.userId == "mockUser") {
                    var needsUpdate = false
                    val updates = hashMapOf<String, Any>()
                    
                    if (issue.lat == null || issue.lng == null) {
                        val (mockLat, mockLng) = when (issue.title) {
                            "Broken Road" -> Pair(6.9200, 79.8794)
                            "Broken Bridge" -> Pair(7.2906, 80.6337)
                            "Broken Traffic light" -> Pair(6.0535, 80.2210)
                            "Road in progress" -> Pair(6.8741, 79.8927)
                            else -> Pair(6.9271, 79.8612)
                        }
                        updates["lat"] = mockLat
                        updates["lng"] = mockLng
                        needsUpdate = true
                    }
                    
                    if (issue.title == "Road in progress" && issue.status != "in-progress") {
                        updates["status"] = "in-progress"
                        needsUpdate = true
                    }
                    
                    if (needsUpdate) {
                        db.collection("issues").document(doc.id).update(updates).await()
                    }
                }
            }
            
            // Seed new active constructions if they don't exist
            val hasFlyover = snap.documents.any { it.getString("title") == "Flyover Work" }
            if (!hasFlyover) {
                val newConstructions = listOf(
                    hashMapOf(
                        "userId" to "mockUser",
                        "userName" to "Admin",
                        "title" to "Flyover Work",
                        "location" to "Bambalapitiya",
                        "description" to "Construction of the new pedestrian flyover.",
                        "photoURL" to "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?q=80&w=600&auto=format&fit=crop",
                        "category" to "Traffic",
                        "status" to "in-progress",
                        "lat" to 6.8970,
                        "lng" to 79.8550,
                        "commentsCount" to 0,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    hashMapOf(
                        "userId" to "mockUser",
                        "userName" to "Admin",
                        "title" to "Drainage Construction",
                        "location" to "Colombo Fort",
                        "description" to "Installing new storm drains along the main road.",
                        "photoURL" to "https://images.unsplash.com/photo-1504307651254-35680f356dfd?q=80&w=600&auto=format&fit=crop",
                        "category" to "Infrastructure",
                        "status" to "in-progress",
                        "lat" to 6.9310,
                        "lng" to 79.8510,
                        "commentsCount" to 0,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                for (item in newConstructions) {
                    db.collection("issues").add(item).await()
                }
            }
        }
    }

    /** Scale a Bitmap so neither dimension exceeds [maxDim], preserving aspect ratio. */
    private fun scaleBitmap(src: Bitmap, maxDim: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxDim && h <= maxDim) return src
        val scale = maxDim.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(src, (w * scale).toInt(), (h * scale).toInt(), true)
    }
}
