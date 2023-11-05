package com.example.foodgram.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R

class CategoryAdapter(private val data: List<CategoryItem>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    data class CategoryItem(val text: String, val imageResId: Int)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryItem = data[position]
        holder.categoryText.text = categoryItem.text
        holder.categoryImage.setImageResource(categoryItem.imageResId)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

