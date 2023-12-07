package com.example.foodgram

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import com.squareup.picasso.Picasso

class UploadedImageAdapter(private val context: Context, private val images: MutableList<Uri>) : BaseAdapter() {

    override fun getCount(): Int = images.size

    override fun getItem(position: Int): Any = images[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_image_uploaded, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val imageUri = images[position]

        // Load image using Picasso
        Picasso.get()
            .load(imageUri)
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            images.removeAt(position)
            notifyDataSetChanged()
        }

        return view
    }

    class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.uploadedImage)
        val deleteButton: ImageButton = view.findViewById(R.id.unUploadImageButton)
    }
}
