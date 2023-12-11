package com.example.foodgram

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected val mediaAccessPermissionCode = 102
    protected val mapSelectionRequestCode = 103

    fun setupAppBar(appBarID: Int) {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(appBarID)
        supportActionBar?.show()
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }


    open fun clearFocusFromAllForms() {}

    // Hide keyboard on screen touch
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            clearFocusFromAllForms()
            hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

}
