package com.example.foodgram.addReview

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.foodgram.AuthManager
import com.example.foodgram.BaseActivity
import com.example.foodgram.Database
import com.example.foodgram.MainActivity
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.databinding.ActivityAddBinding
import com.example.foodgram.review.Review
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AddActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBinding

    private var isUpdateMode: Boolean = false
    private var reviewIDtoUpdate: String = ""

    // Views
    private lateinit var cancelButton: TextView
    private lateinit var postButton: Button
    private lateinit var clearLocationButton: TextView
    private lateinit var uploadImagesButton: Button
    private lateinit var mapButton: LottieAnimationView
    private lateinit var updateButton: Button

    private lateinit var loginTitleTextView: TextView
    private lateinit var restaurantLocationAutoComplete: TextView
    private lateinit var restaurantNameTextField: EditText
    private lateinit var foodNameTextField: EditText
    private lateinit var descriptionTextField: EditText
    private lateinit var ratingBar: RatingBar

    // Image Selector
    private var selectedImages = mutableListOf<Uri>()
    private val maxImages = 6
    private lateinit var imageSelector: ImageSelector
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var imageAdapter: UploadedImageAdapter

    // Location tag
    private lateinit var place: Place
    private var mapsManager: MapsManager? = MapsManager()

    // Activity Results
    private lateinit var locationActivityResult: ActivityResultLauncher<Intent>
    private lateinit var imageSelectionActivityResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setMode()
        initViews()
        updateUI()
        addListeners()
        initActivityResultLaunchers()

        mapsManager!!.initializePlacesAPI(this)

        // App Bar
        setupAppBar(R.layout.app_bar_base)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapsManager = null
    }

    private fun setMode() {
        val reviewID = intent.getStringExtra("reviewID")
        if (!reviewID.isNullOrEmpty()) {
            reviewIDtoUpdate = reviewID
            isUpdateMode = true

            Database.getReviewByID(reviewIDtoUpdate) { review ->
                val placeID = review?.placeID ?: ""
                place = Place.builder().setId(placeID).build()
            }
        }
    }

    private fun updateUI() {
        if(isUpdateMode) {
            postButton.visibility = View.GONE
            updateButton.visibility = View.VISIBLE
            loginTitleTextView.text = getString(R.string.edit_your_review)

            updateFields()

        } else {
            postButton.visibility = View.VISIBLE
            updateButton.visibility = View.GONE

            loginTitleTextView.text = getString(R.string.add_a_restaurant_review)
        }
    }

    private fun updateFields() {
        Database.getReviewByID(reviewIDtoUpdate) {review ->
            restaurantNameTextField.setText(review!!.restaurantName)
            foodNameTextField.setText(review.foodName)
            descriptionTextField.setText(review.description)
            ratingBar.rating = review.rating


            mapsManager?.getPlaceDetails(review.placeID) { place ->
                place?.let {
                    restaurantLocationAutoComplete.text = it.address
                }
            }

            selectedImages.clear()
            for (imageUrl in review.imageUrls) {
                val uri = Uri.parse(imageUrl)
                selectedImages.add(uri)
            }
            imageAdapter.notifyDataSetChanged()
        }
    }


    private fun update() {
        loadingStart()

//        // Delete old images
//        Database.getReviewByID(reviewIDtoUpdate) { oldReview ->
//            Database.deleteImagesFromStorage(oldReview!!.imageUrls)
//        }

        lifecycleScope.launch {

            // Upload new images
            val uploadedImageUrls =
                Database.uploadImagesToStorage(selectedImages) { onUploadImagesFail() }

            val review = makeReviewFromInput(uploadedImageUrls)
            review.id = reviewIDtoUpdate

            Database.updateReview(review,
                { onUpdateReviewSuccess() },
                { onUpdateReviewFail() })
        }

    }

    private fun onUpdateReviewSuccess() {
        showSnackbar(getString(R.string.review_updated_success))
        clearFields()
        loadingEnd()

        navigateToMainActivity()
    }

    private fun onUpdateReviewFail() {
        showSnackbar(getString(R.string.update_failed))
        loadingEnd()
    }


    private fun cancel() {
        onBackPressed()
    }

    private fun post() {
        loadingStart()

        lifecycleScope.launch {

            val uploadedImageUrls =
                Database.uploadImagesToStorage(selectedImages) { onUploadImagesFail() }


            val review = makeReviewFromInput(uploadedImageUrls)

            Database.pushReview(review,
                                { onPostReviewSuccess() },
                                { onPostReviewFail() })

            loadingEnd()
        }
    }

    private fun makeReviewFromInput(uploadedImageUrls: List<String>):Review {
        val restaurantName = restaurantNameTextField.text.toString()
        val foodName = foodNameTextField.text.toString()
        val description = descriptionTextField.text.toString()
        val rating = ratingBar.rating.toString().toFloat()
        val location = place.id
        val timestamp = System.currentTimeMillis()
        val username = AuthManager.getCurrentUser()?.displayName!!
        val id = ""

        return Review(
            id,
            username,
            restaurantName,
            foodName,
            description,
            rating,
            location,
            uploadedImageUrls,
            timestamp)
    }

    private fun onPostReviewSuccess() {
        showSnackbar(getString(R.string.review_added_success))
        clearFields()
        loadingEnd()

        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@AddActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onPostReviewFail() {
        showSnackbar(getString(R.string.post_failed))
        loadingEnd()
    }

    private fun onUploadImagesFail() {
        showSnackbar(getString(R.string.upload_failed))
        loadingEnd()
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

    private fun initViews() {
        cancelButton = binding.cancelTextView
        postButton = binding.postButton

        clearLocationButton = binding.clearLocationTextView
        uploadImagesButton = binding.uploadImageButton
        mapButton = binding.mapButton
        updateButton = binding.updateButton

        loginTitleTextView = binding.loginTitleTextView
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

    private fun addListeners() {
        cancelButton.setOnClickListener{ cancel() }
        postButton.setOnClickListener{ post() }
        updateButton.setOnClickListener{ update() }

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
                showSnackbar(getString(R.string.permission_denied))
            }
        }
    }

    private fun initActivityResultLaunchers() {
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
            showSnackbar(getString(R.string.max_images_allowed, maxImages))
        }

        imageSelector = ImageSelector(this, maxImages)
        hideKeyboard()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(updateButton, message, Snackbar.LENGTH_SHORT).show()
    }


    override fun clearFocusFromAllForms() {
        restaurantNameTextField.clearFocus()
        foodNameTextField.clearFocus()
        descriptionTextField.clearFocus()
        restaurantLocationAutoComplete.clearFocus()
    }

    private fun loadingStart() {
        binding.overlay.visibility = View.VISIBLE
        binding.loading.visibility = View.VISIBLE
    }

    private fun loadingEnd() {
        binding.overlay.visibility = View.GONE
        binding.loading.visibility = View.GONE
    }

}