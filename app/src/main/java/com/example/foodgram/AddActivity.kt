package com.example.foodgram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodgram.databinding.ActivityAddBinding
import com.example.foodgram.databinding.ActivityLoginBinding

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupListeners()

        // Hide app bar
        supportActionBar?.hide()
    }

    private fun initializeViews() {

    }

    private fun setupListeners() {

    }


}
