package com.example.foodgram.addReview

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnMapReadyCallback
import android.os.Bundle
import com.example.foodgram.R
import com.google.android.gms.maps.GoogleMap

class MapSelectionActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)


    }

    override fun onMapReady(googleMap: GoogleMap) {

    }
}
