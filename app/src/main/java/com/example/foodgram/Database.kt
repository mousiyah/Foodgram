package com.example.foodgram

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object Database {
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var users: DatabaseReference = database.getReference("users")
    var reviews: DatabaseReference = database.reference.child("reviews")
    var images: StorageReference = storage.reference.child("post_images")
}