package com.example.foodgram

import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.foodgram.databinding.ActivityAddBinding

open class BaseActivity : AppCompatActivity() {

    fun setupAppBar(appBarID: Int) {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(appBarID)
        supportActionBar?.show()
    }

    fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // Hide keyboard on screen touch
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

}
