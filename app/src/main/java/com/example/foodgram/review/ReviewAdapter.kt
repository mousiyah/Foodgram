package com.example.foodgram.review

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

class ReviewAdapter(private val reviewList: List<Review>,
                    private val context: Context,
                    private val placesClient: PlacesClient) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>(){

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val restaurantNameTextView: TextView = itemView.findViewById(R.id.restaurantName)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val locationTextView: TextView = itemView.findViewById(R.id.restaurantLocation)
        private val imageRecyclerView: RecyclerView = itemView.findViewById(R.id.images)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp)

        private val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        fun bind(review: Review) {

            moreButton.setOnClickListener{ openMoreOptions(review.id, it) }

            usernameTextView.text = review.username
            restaurantNameTextView.text = review.restaurantName
            foodNameTextView.text = review.foodName
            descriptionTextView.text = review.description
            ratingBar.rating = review.rating

            //Location
            val request = FetchPlaceRequest.newInstance(review.placeID, listOf(Place.Field.ADDRESS))

            placesClient.fetchPlace(request).addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                locationTextView.text = place.address
            }.addOnFailureListener { exception: Exception ->
                print(exception)
            }

            // Images
            val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            imageRecyclerView.layoutManager = layoutManager
            val imageAdapter = ReviewImageAdapter(review.imageUrls)
            imageRecyclerView.adapter = imageAdapter

            // Time
            val reviewTime = review.timestamp
            val currentTime = System.currentTimeMillis()

            val relativeTime = DateUtils.getRelativeTimeSpanString(
                reviewTime,
                currentTime,
                DateUtils.MINUTE_IN_MILLIS
            )
                timestampTextView.text = relativeTime.toString()

            }
        }

    fun openMoreOptions(reviewID: String?, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.review_options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_review -> {
                    editReview(reviewID)
                    true
                }
                R.id.delete_review -> {
                    deleteReview(reviewID)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun editReview(reviewID: String?) {

    }

    private fun deleteReview(reviewID: String?) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}
