package com.example.foodgram.addReview

/*
    Citing third-party libraries used in this file:

    Picasso
    Developed by Square, Inc. (2013).
    Version: 2.71828 (or the appropriate version used in your project).
    Retrieved from: https://square.github.io/picasso/
    Load image using Picasso library for image loading and manipulation.

*/

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R
import com.squareup.picasso.Picasso


class UploadedImageAdapter(private val context: Context, private val images: MutableList<Uri>) :
    RecyclerView.Adapter<UploadedImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_uploaded, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = images[position]

        Picasso.get()
            .load(imageUri)
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            images.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = images.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.uploadedImage)
        val deleteButton: ImageButton = view.findViewById(R.id.unUploadImageButton)
    }
}
