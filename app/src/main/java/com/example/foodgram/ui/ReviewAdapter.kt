package com.example.foodgram.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R

class ReviewAdapter(private val context: Context, private val reviewList: List<ReviewItem>) :
    RecyclerView.Adapter<ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val reviewItem = reviewList[position]
        holder.bind(reviewItem)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}

class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Define and initialize your views from item_review.xml here
    // For example: val usernameTextView = itemView.findViewById(R.id.username)
    // You should initialize the views corresponding to the review item's data
    // and populate them in the bind method.

    fun bind(reviewItem: ReviewItem) {
        // Populate your views with data from the reviewItem
        // For example: usernameTextView.text = reviewItem.username
    }
}
