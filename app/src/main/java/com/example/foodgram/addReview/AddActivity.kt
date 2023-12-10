package com.example.foodgram.addReview

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.AuthManager
import com.example.foodgram.BaseActivity
import com.example.foodgram.Database
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.databinding.ActivityAddBinding
import com.example.foodgram.review.Review
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBinding

    // Views
    private lateinit var cancelButton: TextView
    private lateinit var postButton: Button
    private lateinit var clearLocationButton: TextView
    private lateinit var uploadImagesButton: Button
    private lateinit var mapButton: ImageButton

    private lateinit var restaurantLocationAutoComplete: TextView
    private lateinit var restaurantNameTextField: EditText
    private lateinit var foodNameTextField: EditText
    private lateinit var descriptionTextField: EditText
    private lateinit var ratingBar: RatingBar

    // Image Selector
    private val selectedImages = mutableListOf<Uri>()
    private val maxImages = 6
    private lateinit var imageSelector: ImageSelector
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var imageAdapter: UploadedImageAdapter

    // Location tag
    private lateinit var place: Place
    private lateinit var maps: MapsManager

    // Activity Results
    private lateinit var locationActivityResult: ActivityResultLauncher<Intent>
    private lateinit var imageSelectionActivityResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupListeners()

        initializeActivityResultLaunchers()

        maps = MapsManager(this)

        // App Bar
        setupAppBar(R.layout.app_bar_base)
    }

    override fun clearFocusFromAllForms() {
        restaurantNameTextField.clearFocus()
        foodNameTextField.clearFocus()
        descriptionTextField.clearFocus()
        restaurantLocationAutoComplete.clearFocus()
    }

    private fun cancel() {
        onBackPressed()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun post() {
        loadingStart()

        GlobalScope.launch(Dispatchers.Main) {

            val uploadedImageUrls = uploadImagesToStorage()
            pushReviewToDatabase(uploadedImageUrls)

            loadingEnd()
        }
    }

    private fun pushReviewToDatabase(imageUrls: List<String>) {
        val newReviewRef = Database.reviews.push()

        val restaurantName = restaurantNameTextField.text.toString()
        val foodName = foodNameTextField.text.toString()
        val description = descriptionTextField.text.toString()
        val rating = ratingBar.rating.toString().toFloat()
        val location = place.id
        val timestamp = System.currentTimeMillis()
        val username = AuthManager.getCurrentUser()?.displayName!!
        val id = newReviewRef.key

        val review = Review(
            id,
            username,
            restaurantName,
            foodName,
            description,
            rating,
            location,
            imageUrls,
            timestamp)

        newReviewRef.setValue(review)
            .addOnSuccessListener {
                Toast.makeText(this@AddActivity,
                    getString(R.string.review_added_success),
                    Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this@AddActivity,
                    getString(R.string.post_failed),
                    Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun uploadImagesToStorage(): List<String>{
        val uploadedImageUrls = mutableListOf<String>()

        if (selectedImages.isNotEmpty()) {
            for (imageUri in selectedImages) {
                val imageName = UUID.randomUUID().toString()
                val imageRef = Database.images.child("$imageName.jpg")

                try {
                    imageRef.putFile(imageUri).await()

                    val uri = imageRef.downloadUrl.await()
                    uploadedImageUrls.add(uri.toString())
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AddActivity,
                        getString(R.string.upload_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return uploadedImageUrls
    }

    private fun clearFields() {
        restaurantNameTextField.setText("")
        foodNameTextField.setText("")
        descriptionTextField.setText("")
        ratingBar.rating = 0.0f
        restaurantLocationAutoComplete.text = ""
        selectedImages.clear()
        imageAdapter.notifyDataSetChanged()
    }

    private fun clearLocation() {
        restaurantLocationAutoComplete.text = ""
    }

    private fun uploadImages() {
        clearFocusFromAllForms()

        if (hasMediaImagesPermission()) {
            val intent = imageSelector.createGetContentIntent()
            imageSelectionActivityResult.launch(intent)
        } else {
            requestMediaImagesPermission()
        }
    }

    private fun selectOnMap() {
        //TODO
    }

    private fun startLocationAutocomplete() {
        clearFocusFromAllForms()

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)

        locationActivityResult.launch(intent)
    }

    private fun initializeViews() {
        setLoading(binding.loading)

        cancelButton = binding.cancelTextView
        postButton = binding.postButton

        clearLocationButton = binding.clearLocationTextView
        uploadImagesButton = binding.uploadImageButton
        mapButton = binding.mapButton

        restaurantNameTextField = binding.restaurantNameEditText
        foodNameTextField = binding.foodNameEditText
        descriptionTextField = binding.reviewDescriptionEditText

        restaurantLocationAutoComplete = binding.restaurantLocationAutoComplete

        ratingBar = binding.ratingBar

        imageSelector = ImageSelector(this, maxImages)
        imagesRecyclerView = binding.uploadedImagesRecyclerView
        imageAdapter = UploadedImageAdapter(this, selectedImages)
        imagesRecyclerView.adapter = imageAdapter
        imagesRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener{ cancel() }
        postButton.setOnClickListener{ post() }

        clearLocationButton.setOnClickListener{ clearLocation() }
        uploadImagesButton.setOnClickListener{ uploadImages() }
        mapButton.setOnClickListener{ selectOnMap() }

        restaurantLocationAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                startLocationAutocomplete()
            }
        }
        restaurantLocationAutoComplete.setOnClickListener { startLocationAutocomplete() }
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
            mediaAccessPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == mediaAccessPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadImages()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initializeActivityResultLaunchers() {
        locationActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                onActivitySelectLocation(result.resultCode, data)
            }
        }

        imageSelectionActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                onActivitySelectImages(result.resultCode, data)
            }
        }
    }


    private fun onActivitySelectLocation(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                place = Autocomplete.getPlaceFromIntent(data!!)
                val address = place.address

                restaurantLocationAutoComplete.text = address

                if (!place.name.isNullOrBlank()) {
                    restaurantNameTextField.setText(place.name)
                }

                restaurantLocationAutoComplete.clearFocus()
                hideKeyboard()
            }
            AutocompleteActivity.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(data!!)
                restaurantLocationAutoComplete.clearFocus()
                hideKeyboard()
            }
            AutocompleteActivity.RESULT_CANCELED -> {
                restaurantLocationAutoComplete.clearFocus()
                hideKeyboard()
            }
        }
    }

    private fun onActivitySelectImages(resultCode: Int, data: Intent?) {
        val newSelectedImages =
            imageSelector.handleSelectedImages(resultCode, data)

        if (selectedImages.size + newSelectedImages.size <= maxImages) {
            selectedImages.addAll(newSelectedImages)
            imageAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this,
                getString(R.string.max_images_allowed, maxImages),
                Toast.LENGTH_SHORT).show()
        }

        imageSelector = ImageSelector(this, maxImages)
        hideKeyboard()
    }


    private fun mapSelectionComplete(placeFromMap: Place, placeName: String?, placeAddress: String?) {
        place = placeFromMap
        restaurantNameTextField.setText(placeName)
        restaurantLocationAutoComplete.setText(placeAddress)
    }

}