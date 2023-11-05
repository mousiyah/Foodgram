package com.example.foodgram.ui

data class ReviewItem(
    val username: String,
    val location: String,
    val tags: String,
    val description: String,
    val starRating: Float,
    val timeAgo: String,
    val imageList: List<Int> // List of image resource IDs
)
