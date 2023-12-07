package com.example.foodgram

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.foodgram.databinding.ActivityAddBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class AddActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBinding

    private lateinit var cancelButton: TextView
    private lateinit var postButton: Button

    private lateinit var clearLocationButton: TextView
    private lateinit var uploadImagesButton: Button

    private lateinit var restaurantLocationAutoComplete: TextView
    private lateinit var restaurantNameTextField: EditText
    private lateinit var foodNameTextField: EditText
    private lateinit var reviewContentTextField: EditText

    private lateinit var ratingBar: RatingBar

    private val maxImages = 3
    private lateinit var imageSelector: ImageSelector
    private lateinit var imageGridView: GridView
    private lateinit var imageAdapter: UploadedImageAdapter
    private val selectedImages = mutableListOf<Uri>()

    private val PERMISSION_CODE = 1001
    private val mapsAPIkey = "AIzaSyD56CgJp9BOrChCvQh3h6l9-VJGX7hRqZk"

    // Places API
    private lateinit var placesClient: PlacesClient
    private val AUTOCOMPLETE_REQUEST_CODE = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupListeners()

        initializePlacesAPK()

        // App Bar
        setupAppBar(R.layout.app_bar_base)
    }

    private fun initializeViews() {
        cancelButton = binding.cancelTextView
        postButton = binding.postButton

        clearLocationButton = binding.clearLocationTextView
        uploadImagesButton = binding.uploadImageButton

        restaurantNameTextField = binding.restaurantNameEditText
        foodNameTextField = binding.foodNameEditText
        reviewContentTextField = binding.reviewContentEditText

        restaurantLocationAutoComplete = binding.restaurantLocationAutoComplete

        ratingBar = binding.ratingBar

        imageSelector = ImageSelector(this, maxImages)
        imageGridView = binding.uploadedImagesGridView
        imageAdapter = UploadedImageAdapter(this, selectedImages)
        imageGridView.adapter = imageAdapter
    }

    private fun setupListeners() {

        cancelButton.setOnClickListener{ cancel() }
        postButton.setOnClickListener{ post() }

        clearLocationButton.setOnClickListener{ clearLocation() }
        uploadImagesButton.setOnClickListener{ uploadImages() }

        restaurantLocationAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                startPlaceAutocomplete()
            }
        }
        restaurantLocationAutoComplete.setOnClickListener { startPlaceAutocomplete() }
    }

    private fun cancel() {
        onBackPressed()
    }

    private fun post() {

    }

    private fun clearLocation() {
        restaurantLocationAutoComplete.text = ""
    }

    private fun initializePlacesAPK() {
        Places.initialize(applicationContext, mapsAPIkey) // Replace with your API key
        placesClient = Places.createClient(this)
    }

    private fun startPlaceAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun uploadImages() {
        if (hasMediaImagesPermission()) {
            // Permission already granted, proceed with image selection
            imageSelector.selectImages()
        } else {
            // Request permission if not granted
            requestMediaImagesPermission()
        }
    }

    private fun hasMediaImagesPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            "android.permission.READ_MEDIA_IMAGES"
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMediaImagesPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf("android.permission.READ_MEDIA_IMAGES"),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with image selection
                imageSelector.selectImages()
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(
                    this,
                    "Permission denied, cannot access media images",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                AutocompleteActivity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val address = place.address
                    val latLng: LatLng? = place.latLng

                    // Handle the retrieved details, like setting them to the EditText
                    restaurantLocationAutoComplete.text = address
                    restaurantLocationAutoComplete.clearFocus()
                    hideKeyboard()
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    // Handle error
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        } else {

            val newSelectedImages =
                imageSelector.handleSelectedImages(requestCode, resultCode, data)

            if (selectedImages.size + newSelectedImages.size <= maxImages) {
                selectedImages.addAll(newSelectedImages)
                imageAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Maximum $maxImages images allowed", Toast.LENGTH_SHORT).show()
            }

            imageSelector = ImageSelector(this, maxImages)
        }

    }


}
