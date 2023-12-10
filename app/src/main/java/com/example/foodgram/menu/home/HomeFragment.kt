package com.example.foodgram.menu.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.addReview.AddActivity
import com.example.foodgram.AuthManager
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentHomeBinding
import com.example.foodgram.review.ReviewAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var addButton: FloatingActionButton

    private lateinit var reviewsRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initializeViews()
        setupListeners()
        updateUI()

        reviewsRecyclerView = binding.reviewsRecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initializeViews() {
        addButton = binding.addButton
    }

    private fun setupListeners() {
        addButton.setOnClickListener { addButtonClicked() }
    }

    private fun updateUI() {
        if (AuthManager.isGuestMode()){
            addButton.visibility = View.GONE
        } else{
            addButton.visibility = View.VISIBLE
        }
    }

    private fun addButtonClicked() {
        startActivity(Intent(requireContext(), AddActivity::class.java))
    }


}
