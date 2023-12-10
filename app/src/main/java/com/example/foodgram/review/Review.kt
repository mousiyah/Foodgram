package com.example.foodgram.review

data class Review(
    var id: String? = "",
    var username: String = "",
    var restaurantName: String = "",
    var foodName: String = "",
    var description: String = "",
    var rating: Float = 0.0f,
    var placeID: String = "",
    var imageUrls: List<String> = listOf(),
    val timestamp: Long = 0
)

