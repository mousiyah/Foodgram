package com.example.foodgram

import android.content.Context
import android.content.res.Resources.getSystem
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.example.foodgram.R


class MapsManager() {

    private val mapsAPIkey = "AIzaSyD56CgJp9BOrChCvQh3h6l9-VJGX7hRqZk"
    var placesClient: PlacesClient? = null

    fun initializePlacesAPI(applicationContext: Context) {
        Places.initialize(applicationContext, mapsAPIkey)
        placesClient = Places.createClient(applicationContext)
    }

    fun getPlaceDetails(placeID: String, callback: (Place?) -> Unit) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
        val placeRequest = FetchPlaceRequest.newInstance(placeID, placeFields)

        placesClient?.fetchPlace(placeRequest)?.addOnSuccessListener { response: FetchPlaceResponse ->
            val place = response.place
            callback(place)

        }?.addOnFailureListener { exception: Exception ->
            callback(null)
            print(exception)
        }
    }


}