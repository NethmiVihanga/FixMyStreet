package com.fixmystreet.data.repository

import android.app.Activity
import com.fixmystreet.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun register(name: String, email: String, phone: String, pass: String) {
        val cleanEmail = email.trim().lowercase()
        // Enforce uniqueness checks: email and number can only make one account
        val emailsToQuery = listOf(cleanEmail, email.trim(), email).distinct()
        val emailQuery = db.collection("users").whereIn("email", emailsToQuery).get().await()
        if (!emailQuery.isEmpty) {
            throw Exception("This Email is already registered with another account.")
        }
        
        val phoneQuery = db.collection("users").whereEqualTo("phone", phone).get().await()
        if (!phoneQuery.isEmpty) {
            throw Exception("This Phone Number is already registered with another account.")
        }

        val secureAuthPassword = "Auth_" + cleanEmail.hashCode().toString() + "_Secure!"
        val res = auth.createUserWithEmailAndPassword(cleanEmail, secureAuthPassword).await()
        val uid = res.user?.uid ?: return
        
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to cleanEmail,
            "phone" to phone,
            "password" to pass, // Store real password for verification/reset sync
            "photoURL" to "",
            "bio" to "",
            "issuesCount" to 0,
            "resolvedCount" to 0,
            "deactivated" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).set(user).await()
    }

    suspend fun login(email: String, pass: String) {
        val cleanEmail = email.trim().lowercase()
        val emailsToQuery = listOf(cleanEmail, email.trim(), email).distinct()
        val query = db.collection("users").whereIn("email", emailsToQuery).get().await()

        val standardSecurePassword = "Auth_" + cleanEmail.hashCode().toString() + "_Secure!"
        
        // Build a list of candidate secure passwords to try, standard first
        val possibleSecurePasswords = mutableListOf<String>()
        possibleSecurePasswords.add(standardSecurePassword)
        
        // Add other permutations from typed email
        listOf(email.trim(), email, email.lowercase()).forEach { perm ->
            val hashPass = "Auth_" + perm.hashCode().toString() + "_Secure!"
            if (!possibleSecurePasswords.contains(hashPass)) {
                possibleSecurePasswords.add(hashPass)
            }
        }
        
        var matchedDocEmail: String? = null
        var storedPass: String? = null
        var previousPassword: String? = null
        
        if (!query.isEmpty) {
            val doc = query.documents[0]
            matchedDocEmail = doc.getString("email")
            storedPass = doc.getString("password")
            previousPassword = doc.getString("previousPassword")
            
            // Also add permutations from matched doc email if different
            matchedDocEmail?.let { docEmail ->
                val docClean = docEmail.trim().lowercase()
                listOf(docClean, docEmail.trim(), docEmail, docEmail.lowercase()).forEach { perm ->
                    val hashPass = "Auth_" + perm.hashCode().toString() + "_Secure!"
                    if (!possibleSecurePasswords.contains(hashPass)) {
                        possibleSecurePasswords.add(hashPass)
                    }
                }
            }
        }

        val authEmail = matchedDocEmail ?: cleanEmail

        if (storedPass != null) {
            if (storedPass == pass) {
                // The plaintext password matches what is stored in Firestore!
                // Try logging into Firebase Auth with candidate secure passwords
                var loginSuccess = false
                var activeSecurePassword = standardSecurePassword

                for (securePass in possibleSecurePasswords) {
                    try {
                        auth.signInWithEmailAndPassword(authEmail, securePass).await()
                        loginSuccess = true
                        activeSecurePassword = securePass
                        break
                    } catch (e: Exception) {
                        // Try next secure password
                    }
                }

                if (loginSuccess) {
                    // Successfully logged in! If we didn't use the standard secure password, migrate it now!
                    if (activeSecurePassword != standardSecurePassword) {
                        try {
                            auth.currentUser?.updatePassword(standardSecurePassword)?.await()
                        } catch (migrationEx: Exception) {
                            // Non-fatal migration error
                        }
                    }
                    return
                } else {
                    // All deterministic secure password attempts failed.
                    // Firestore CONFIRMED the password is correct — Firebase Auth account
                    // is either missing or has an unknown/out-of-sync password.

                    // Step 1: Try the plaintext entered password directly in Firebase Auth.
                    try {
                        val res = auth.signInWithEmailAndPassword(authEmail, pass).await()
                        try {
                            res.user?.updatePassword(standardSecurePassword)?.await()
                        } catch (migrationEx: Exception) {
                            // Non-fatal
                        }
                        return
                    } catch (plainTextEx: Exception) {
                        // Step 2: Try previousPassword (set during the last password reset flow).
                        // This handles the case where Firebase Auth still holds the old password.
                        if (!previousPassword.isNullOrEmpty() && previousPassword != pass) {
                            try {
                                val res = auth.signInWithEmailAndPassword(authEmail, previousPassword).await()
                                try { res.user?.updatePassword(standardSecurePassword)?.await() } catch (e: Exception) {}
                                return
                            } catch (prevEx: Exception) {
                                // Previous password also rejected — continue
                            }
                        }

                        // Step 3: Try to recreate the Firebase Auth account.
                        // This is the fix for the common case where Firebase's email-enumeration-
                        // protection caused sendPasswordResetEmail to silently "succeed" in the
                        // reset flow even though no account existed, leaving Auth empty.
                        try {
                            auth.createUserWithEmailAndPassword(authEmail, standardSecurePassword).await()
                            // Successfully recreated — user is now logged in
                            return
                        } catch (createEx: Exception) {
                            // Step 4: Account genuinely exists with an unknown password.
                            // Send a reset email silently and give the user a clear action.
                            try { auth.sendPasswordResetEmail(authEmail).await() } catch (e: Exception) {}
                            throw Exception("We've sent a re-sync link to $authEmail. Please check your inbox (and spam folder), click the link, then log in again.")
                        }
                    }
                }
            } else {
                // Firestore password doesn't match — but the user may have reset via a native Firebase email link,
                // which updates Firebase Auth directly (bypassing Firestore). Try self-healing:
                // Attempt to sign in with the entered password directly in Firebase Auth.
                try {
                    val res = auth.signInWithEmailAndPassword(authEmail, pass).await()
                    // Self-heal: update Firestore to reflect the new password the user confirmed
                    val uid = res.user?.uid
                    if (uid != null) {
                        try {
                            db.collection("users").document(uid).update("password", pass).await()
                        } catch (firestoreEx: Exception) {
                            // Non-fatal: Firestore update failed but user is authenticated
                        }
                        // Migrate Firebase Auth to standard secure password
                        try {
                            res.user?.updatePassword(standardSecurePassword)?.await()
                            // Also update Firestore to reflect the migrated standard state
                            db.collection("users").document(uid).update("password", pass).await()
                        } catch (migrationEx: Exception) {
                            // Non-fatal
                        }
                    }
                    return
                } catch (directLoginEx: Exception) {
                    // Direct login with entered password also failed — truly wrong password
                    throw Exception("Incorrect password.")
                }
            }
        }

        // If user record is not found in Firestore or storedPass is null, fall back to direct Firebase Auth checks
        var firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            var loginSuccess = false
            var activeSecurePassword = standardSecurePassword
            
            for (securePass in possibleSecurePasswords) {
                try {
                    val res = auth.signInWithEmailAndPassword(authEmail, securePass).await()
                    firebaseUser = res.user
                    loginSuccess = true
                    activeSecurePassword = securePass
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }
            
            if (!loginSuccess) {
                // Try plaintext pass directly
                try {
                    val res = auth.signInWithEmailAndPassword(authEmail, pass).await()
                    firebaseUser = res.user
                    // Migrate this legacy user to deterministic password on the fly
                    try {
                        firebaseUser?.updatePassword(standardSecurePassword)?.await()
                    } catch (migrationEx: Exception) {
                        // Non-fatal
                    }
                } catch (e2: Exception) {
                    throw Exception("Login failed: Incorrect email or password.")
                }
            } else {
                // If standard wasn't used, migrate it
                if (activeSecurePassword != standardSecurePassword) {
                    try {
                        firebaseUser?.updatePassword(standardSecurePassword)?.await()
                    } catch (migrationEx: Exception) {
                        // Non-fatal
                    }
                }
            }
        }

        // Auto-heal missing Firestore user records on the fly
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                val user = hashMapOf(
                    "uid" to uid,
                    "name" to (firebaseUser.displayName ?: authEmail.substringBefore("@")),
                    "email" to authEmail,
                    "phone" to "",
                    "password" to pass,
                    "photoURL" to (firebaseUser.photoUrl?.toString() ?: ""),
                    "bio" to "",
                    "issuesCount" to 0,
                    "resolvedCount" to 0,
                    "deactivated" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(user).await()
            }
        }
    }

    suspend fun signInWithSocialGracefully(email: String, name: String, providerId: String) {
        if (email.isBlank()) throw Exception("Email cannot be empty.")
        val cleanEmail = email.trim().lowercase()
        val standardSecurePassword = "Auth_" + cleanEmail.hashCode().toString() + "_Secure!"

        // Build a list of candidate secure passwords to try, standard first
        val possibleSecurePasswords = mutableListOf<String>()
        possibleSecurePasswords.add(standardSecurePassword)
        
        // Add other permutations from typed/social email
        listOf(email.trim(), email, email.lowercase()).forEach { perm ->
            val hashPass = "Auth_" + perm.hashCode().toString() + "_Secure!"
            if (!possibleSecurePasswords.contains(hashPass)) {
                possibleSecurePasswords.add(hashPass)
            }
        }

        // 1. Check if user already registered in Firestore
        val emailsToQuery = listOf(cleanEmail, email.trim(), email).distinct()
        val query = db.collection("users").whereIn("email", emailsToQuery).get().await()
        if (!query.isEmpty) {
            val doc = query.documents[0]
            val matchedDocEmail = doc.getString("email") ?: cleanEmail
            
            // Also add permutations from matched doc email if different
            listOf(matchedDocEmail.trim().lowercase(), matchedDocEmail.trim(), matchedDocEmail, matchedDocEmail.lowercase()).forEach { perm ->
                val hashPass = "Auth_" + perm.hashCode().toString() + "_Secure!"
                if (!possibleSecurePasswords.contains(hashPass)) {
                    possibleSecurePasswords.add(hashPass)
                }
            }

            // Authenticate using deterministic password
            var loginSuccess = false
            var activeSecurePassword = standardSecurePassword
            
            for (securePass in possibleSecurePasswords) {
                try {
                    auth.signInWithEmailAndPassword(matchedDocEmail, securePass).await()
                    loginSuccess = true
                    activeSecurePassword = securePass
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }
            
            if (!loginSuccess) {
                // If Auth fails but user is in DB, try to create standard user session
                try {
                    auth.createUserWithEmailAndPassword(matchedDocEmail, standardSecurePassword).await()
                } catch (createEx: Exception) {
                    // Fall back to sign in with standard
                    auth.signInWithEmailAndPassword(matchedDocEmail, standardSecurePassword).await()
                }
            } else {
                // Migrate if not standard
                if (activeSecurePassword != standardSecurePassword) {
                    try {
                        auth.currentUser?.updatePassword(standardSecurePassword)?.await()
                    } catch (migrationEx: Exception) {
                        // Non-fatal
                    }
                }
            }
        } else {
            // 2. Register new social user deterministically
            var uid: String? = null
            try {
                val res = auth.createUserWithEmailAndPassword(cleanEmail, standardSecurePassword).await()
                uid = res.user?.uid
            } catch (createEx: Exception) {
                // Already registered in standard Auth but missing Firestore, try sign in using permutations
                var loginSuccess = false
                var activeSecurePassword = standardSecurePassword
                
                for (securePass in possibleSecurePasswords) {
                    try {
                        val res = auth.signInWithEmailAndPassword(cleanEmail, securePass).await()
                        uid = res.user?.uid
                        loginSuccess = true
                        activeSecurePassword = securePass
                        break
                    } catch (signInEx: Exception) {
                        // Try next
                    }
                }
                
                if (!loginSuccess) {
                    // Fallback create or signin
                    val res = auth.signInWithEmailAndPassword(cleanEmail, standardSecurePassword).await()
                    uid = res.user?.uid
                } else {
                    if (activeSecurePassword != standardSecurePassword) {
                        try {
                            auth.currentUser?.updatePassword(standardSecurePassword)?.await()
                        } catch (migrationEx: Exception) {
                            // Non-fatal
                        }
                    }
                }
            }

            val finalUid = uid ?: throw Exception("Failed to establish secure session.")
            val user = hashMapOf(
                "uid" to finalUid,
                "name" to name,
                "email" to cleanEmail,
                "phone" to "",
                "password" to "social_login_$providerId",
                "photoURL" to "",
                "bio" to "",
                "issuesCount" to 0,
                "resolvedCount" to 0,
                "deactivated" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(finalUid).set(user).await()
        }
    }

    suspend fun signInWithSocialProvider(activity: Activity, providerId: String) {
        val providerBuilder = OAuthProvider.newBuilder(providerId)
        val res = auth.startActivityForSignInWithProvider(activity, providerBuilder.build()).await()
        
        val firebaseUser = res.user ?: throw Exception("Social login failed: User is null")
        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: ""
        val name = firebaseUser.displayName ?: "Social User"
        val phone = firebaseUser.phoneNumber ?: ""
        
        val userDoc = db.collection("users").document(uid).get().await()
        if (!userDoc.exists()) {
            val user = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "password" to "social_login",
                "photoURL" to (firebaseUser.photoUrl?.toString() ?: ""),
                "bio" to "",
                "issuesCount" to 0,
                "resolvedCount" to 0,
                "deactivated" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(user).await()
        }
    }

    fun logout() {
        auth.signOut()
    }
}
