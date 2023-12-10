package com.example.foodgram.addReview

import android.app.Activity
import android.content.Intent
import android.net.Uri

class ImageSelector(private val activity: Activity, private val maxImages: Int) {
    private val selectedImages = mutableListOf<Uri>()

    fun createGetContentIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        return intent
    }

    fun handleSelectedImages(resultCode: Int, data: Intent?): List<Uri> {
        val images = mutableListOf<Uri>()

        if (resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData

            if (clipData != null) {
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val uri = clipData.getItemAt(i).uri
                    images.add(uri)
                }
            } else {
                val uri = data.data
                uri?.let {
                    images.add(it)
                }
            }
        }

        return images
    }

}