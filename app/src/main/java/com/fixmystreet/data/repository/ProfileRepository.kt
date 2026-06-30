package com.fixmystreet.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.fixmystreet.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun getProfile(userId: String): User? {
        val snap = db.collection("users").document(userId).get().await()
        return snap.toObject(User::class.java)
    }

    suspend fun updateProfile(
        context: Context,
        userId: String,
        name: String,
        bio: String,
        phoneNumber: String,
        avatarUri: Uri?,
        deletePhoto: Boolean = false
    ) {
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "bio" to bio,
            "phone" to phoneNumber,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val reqBuilder = UserProfileChangeRequest.Builder()
            .setDisplayName(name)

        if (deletePhoto) {
            updates["photoURL"] = ""
            reqBuilder.setPhotoUri(null)
            auth.currentUser?.updateProfile(reqBuilder.build())?.await()
        } else if (avatarUri != null) {
            val uriStr = avatarUri.toString()
            val photoUrl = when {
                uriStr.startsWith("http://") || uriStr.startsWith("https://") -> {
                    uriStr
                }
                else -> {
                    // Local file URI — compress with Bitmap API and store as Base64 in Firestore
                    val inputStream = context.contentResolver.openInputStream(avatarUri)
                        ?: throw Exception("Cannot open image file. Please try selecting the photo again.")
                    val rawBytes = inputStream.use { it.readBytes() }
                    if (rawBytes.isEmpty()) throw Exception("Selected image appears to be empty.")

                    // Decode → scale down to max 400px (avatar is small) → compress to JPEG 60%
                    val original = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                        ?: throw Exception("Could not decode image.")
                    val scaled = scaleBitmap(original, maxDim = 400)
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 60, out)
                    val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    "b64:$base64"
                }
            }
            updates["photoURL"] = photoUrl
            if (!photoUrl.startsWith("b64:")) {
                reqBuilder.setPhotoUri(Uri.parse(photoUrl))
            }
            auth.currentUser?.updateProfile(reqBuilder.build())?.await()
        } else {
            auth.currentUser?.updateProfile(reqBuilder.build())?.await()
        }

        db.collection("users").document(userId).update(updates).await()
    }

    suspend fun deactivateAccount(userId: String) {
        db.collection("users").document(userId).update(
            "deactivated", true,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
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

