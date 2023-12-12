package com.example.foodgram

import android.net.Uri
import com.example.foodgram.review.Review
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseRegistrar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

object Database {
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var images: StorageReference = storage.reference.child("post_images")

    var users: DatabaseReference = database.getReference("users")
    var reviews: DatabaseReference = database.reference.child("reviews")

    val myReviewsQuery = reviews.orderByChild("username").equalTo(AuthManager.getUsername())

    var savedReviews: DatabaseReference? = null

    fun setUpDatabase() {
        savedReviews = AuthManager.getUsername()?.let { username ->
            if (!AuthManager.isGuestMode()) {
                users.child(username).child("saved")
            } else {
                null
            }
        }
    }

    fun getReviewByID(id: String, callback: (Review?) -> Unit) {
        reviews.child(id).get().addOnSuccessListener { snapshot ->
            val review = snapshot.getValue(Review::class.java)
            callback(review)
        }.addOnFailureListener { exception ->
            print(exception)
            callback(null)
        }
    }

    fun pushReview(
        review: Review,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val newReviewRef = reviews.push()
        review.id = newReviewRef.key

        newReviewRef.setValue(review)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    suspend fun uploadImagesToStorage(selectedImages: MutableList<Uri>,
                                      onUploadImagesFail: () -> Unit): List<String>{
        val uploadedImageUrls = mutableListOf<String>()

        if (selectedImages.isNotEmpty()) {
            for (imageUri in selectedImages) {

                if (!isUrl(imageUri)) {
                    val imageName = UUID.randomUUID().toString()
                    val imageRef = images.child("$imageName.jpg")

                    try {
                        imageRef.putFile(imageUri).await()

                        val url = imageRef.downloadUrl.await()
                        uploadedImageUrls.add(url.toString())
                    } catch (e: Exception) {
                        onUploadImagesFail
                    }
                } else {
                    uploadedImageUrls.add(imageUri.toString())
                }

            }
        }

        return uploadedImageUrls
    }

    fun isUrl(uri: Uri): Boolean {
        val uriString = uri.toString()
        return try {
            val uri = Uri.parse(uriString)
            uri.scheme != null && (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true))
        } catch (e: Exception) {
            false
        }
    }

    fun deleteReview(id: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        getReviewByID(id) {
            deleteImagesFromStorage(it?.imageUrls ?: return@getReviewByID)
        }
        reviews.child(id).removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: () -> Unit) {
        reviews.child(review.id!!).setValue(review)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun deleteImagesFromStorage(imageUrls: List<String>) {
        if (imageUrls.isNotEmpty()) {
            for (imageUrl in imageUrls) {
                val imageRef = storage.getReferenceFromUrl(imageUrl)

                imageRef.delete()
            }
        }
    }

    fun saveReview(id: String) {
        val userRef = users.child(AuthManager.getUsername()!!)
        userRef.child("saved").push().setValue(id)
    }

    fun unSaveReview(id: String) {
        val query = savedReviews?.orderByValue()?.equalTo(id)

        if (query != null) {
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    print(error)
                }

            })
        }
    }

}