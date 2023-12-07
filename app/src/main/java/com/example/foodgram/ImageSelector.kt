package com.example.foodgram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class ImageSelector(private val activity: Activity, private val maxImages: Int) {
    private val selectedImages = mutableListOf<Uri>()

    fun selectImages() {
        val intent = Intent().apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
        }
        activity.startActivityForResult(
            Intent.createChooser(intent, "Select Images"),
            PICK_IMAGES_REQUEST
        )
    }

    fun handleSelectedImages(requestCode: Int, resultCode: Int, data: Intent?): List<Uri> {
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                intent.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        if (selectedImages.size < maxImages) {
                            val imageUri: Uri = clipData.getItemAt(i).uri
                            selectedImages.add(imageUri)
                        } else {
                            showToast("Maximum $maxImages images allowed")
                            break
                        }
                    }
                } ?: intent.data?.let { imageUri ->
                    selectedImages.add(imageUri)
                }
            }
        }
        return selectedImages.toList()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGES_REQUEST = 1
    }
}
