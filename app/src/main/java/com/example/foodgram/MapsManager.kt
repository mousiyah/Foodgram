package com.example.foodgram

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MapsManager(applicationContext: Context) {

    private val mapsAPIkey = "AIzaSyD56CgJp9BOrChCvQh3h6l9-VJGX7hRqZk"
    public lateinit var placesClient: PlacesClient

    init {
        initializePlacesAPI(applicationContext)
    }

    private fun initializePlacesAPI(applicationContext: Context) {
        Places.initialize(applicationContext, mapsAPIkey)
        placesClient = Places.createClient(applicationContext)
    }


}