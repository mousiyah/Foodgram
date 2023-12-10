package com.example.foodgram

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
        checkUsernameAvailability(username) { isAvailable ->
            if (isAvailable) {
                createUserAndProfile(email, password, username, callback)
            } else {
                callback(false)
            }
        }
    }

    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        Database.users.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(!dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun createUserAndProfile(email: String, password: String, username: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { createUserTask ->
                if (createUserTask.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        updateUserProfile(currentUser, username) { profileUpdated ->
                            if (profileUpdated) {
                                saveUserData(email, username, currentUser.uid) { userDataSaved ->
                                    if (userDataSaved) {
                                        callback(true)
                                    } else {
                                        callback(false)
                                    }
                                }
                            } else {
                                callback(false)
                            }
                        }
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, username: String, callback: (Boolean) -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { updateProfileTask ->
                callback(updateProfileTask.isSuccessful)
            }
    }

    private fun saveUserData(email: String, username: String, uid: String, callback: (Boolean) -> Unit) {
        val userData = mapOf(
            "email" to email,
            "uid" to uid
        )

        Database.users.child(username).setValue(userData)
            .addOnCompleteListener { saveUserTask ->
                callback(saveUserTask.isSuccessful)
            }
    }

}
