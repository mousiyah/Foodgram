package com.example.foodgram.review

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.AuthManager
import com.example.foodgram.Database
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.addReview.AddActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi

class ReviewAdapter(private val reviewList: List<Review>,
                    private val context: Context, private val mapsManager: MapsManager
) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>(){

    // 'Favourite' icon drawables
    val icFavSelected = ContextCompat.getDrawable(context, R.drawable.ic_favourite)
    val icFavBorder = ContextCompat.getDrawable(context, R.drawable.ic_favourite_border)

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Views
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val restaurantNameTextView: TextView = itemView.findViewById(R.id.restaurantName)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val locationTextView: TextView = itemView.findViewById(R.id.restaurantLocation)
        private val imageRecyclerView: RecyclerView = itemView.findViewById(R.id.images)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp)

        // Clickable
        private val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)
        private val saveButton: LinearLayout = itemView.findViewById(R.id.saveButton)
        private val saveImage: ImageView = itemView.findViewById(R.id.saveImage)


        @OptIn(DelicateCoroutinesApi::class)
        fun bind(review: Review) {
            setUpUI(review)
        }

        private fun setUpUI(review: Review) {
            setUpInteraction(review)
            setUpViews(review)
        }

        private fun setUpInteraction(review: Review) {
            if (AuthManager.isGuestMode()) {
                saveButton.visibility = View.GONE
                moreButton.visibility = View.GONE
            } else {


                if (review.username == AuthManager.getUsername()) {

                    moreButton.visibility = View.VISIBLE
                    saveButton.visibility = View.GONE

                    moreButton.setOnClickListener { openMoreOptions(review.id, it, context) }

                } else {

                    moreButton.visibility = View.GONE
                    saveButton.visibility = View.VISIBLE

                    setSaveButtonImage(review.id, saveImage)
                    saveButton.setOnClickListener{ saveReview(review.id, saveImage)}

                }
            }
        }


        private fun setUpViews(review: Review) {

            // General fields
            usernameTextView.text = review.username
            restaurantNameTextView.text = review.restaurantName
            foodNameTextView.text = review.foodName
            descriptionTextView.text = review.description
            ratingBar.rating = review.rating

            // Location
            mapsManager.getPlaceDetails(review.placeID) { place ->
                place?.let {
                    locationTextView.text = it.address
                }
            }

            // Images
            val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            imageRecyclerView.layoutManager = layoutManager
            val imageAdapter = ReviewImageAdapter(review.imageUrls)
            imageRecyclerView.adapter = imageAdapter

            timestampTextView.text = getRelativeTime(review.timestamp)

        }

        private fun getRelativeTime(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()

            val relativeTime = DateUtils.getRelativeTimeSpanString(
                timestamp,
                currentTime,
                DateUtils.MINUTE_IN_MILLIS
            )

            return relativeTime.toString()
        }


    }


    fun openMoreOptions(reviewID: String?, view: View, context: Context) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.review_options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_review -> {
                    editReview(reviewID, context)
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

    fun saveReview(reviewID: String?, saveImage: ImageView) {

        if (saveImage.tag == true) {

            saveImage.setImageDrawable(icFavBorder)
            saveImage.tag = false

            Database.unSaveReview(reviewID!!)

        } else {

            saveImage.setImageDrawable(icFavSelected)
            saveImage.tag = true

            Database.saveReview(reviewID!!)

        }
    }

    private fun editReview(reviewID: String?, context: Context) {
        val intent = Intent(context, AddActivity::class.java)
        intent.putExtra("reviewID", reviewID)
        startActivity(context, intent, null)
    }

    private fun deleteReview(reviewID: String?) {
        Database.deleteReview(reviewID!!,
            { notifyDataSetChanged() },
            { Toast.makeText(context, context.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()})
    }

    private fun setSaveButtonImage(id: String?, saveImage: ImageView) {
        Database.savedReviews?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isSaved = dataSnapshot.children.any { it.getValue(String::class.java) == id }
                if (isSaved) {
                    saveImage.setImageDrawable(icFavSelected)
                    saveImage.tag = true
                } else {
                    saveImage.setImageDrawable(icFavBorder)
                    saveImage.tag = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print(error)
            }
        })
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
