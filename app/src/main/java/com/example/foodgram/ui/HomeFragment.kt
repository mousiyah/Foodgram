package com.example.foodgram.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.AddActivity
import com.example.foodgram.AuthManager
import com.example.foodgram.MainActivity
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var addButton: FloatingActionButton

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewList: List<ReviewItem>


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

        populateReviews()
            reviewAdapter = ReviewAdapter(requireContext(), reviewList)
        reviewsRecyclerView.adapter = reviewAdapter

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

    private fun populateReviews(){
        reviewList = listOf(
            ReviewItem(
                username = "azra.lin",
                location = "Sushi bar & restaurant, Swansea",
                tags = "#sushi",
                description = "The best sushi place I have ever been to. Good for family gatherings. Portions are good size and they served us free tea 🍣☕️ Definitely will come back again",
                starRating = 4.5f, // 4.5 stars
                timeAgo = "1h ago",
                imageList = listOf(
                    R.drawable.p1, // Sample image resource IDs
                    R.drawable.p2,
                    R.drawable.p3
                )
            ),
            ReviewItem(
                username = "azra.lin",
                location = "Sushi bar & restaurant, Swansea",
                tags = "#sushi",
                description = "The best sushi place I have ever been to. Good for family gatherings. Portions are good size and they served us free tea 🍣☕️ Definitely will come back again",
                starRating = 4.5f, // 4.5 stars
                timeAgo = "1h ago",
                imageList = listOf(
                    R.drawable.p1, // Sample image resource IDs
                    R.drawable.p2,
                    R.drawable.p3
                )
            ),
            // Add more ReviewItem objects as needed
        )

    }
}
