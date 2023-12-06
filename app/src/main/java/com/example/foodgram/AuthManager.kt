package com.example.foodgram

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getUsername(): String? {
        return auth.currentUser?.displayName
    }

    fun isGuestMode(): Boolean {
        return getCurrentUser() == null
    }

    fun signOut() {
        auth.signOut()
    }

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun register(email: String, password: String, username: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let { firebaseUser ->
                        // Update user profile with the username
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateProfileTask ->
                                if (updateProfileTask.isSuccessful) {
                                    callback(true)
                                } else {
                                    callback(false)
                                }
                            }
                    }
                } else {
                    callback(false)
                }
            }
    }

}
