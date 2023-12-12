package com.example.foodgram.menu.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.addReview.AddActivity
import com.example.foodgram.AuthManager
import com.example.foodgram.Database
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentHomeBinding
import com.example.foodgram.review.Review
import com.example.foodgram.review.ReviewAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var addButton: FloatingActionButton

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    private var valueEventListener: ValueEventListener? = null
    private var mapsManager: MapsManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        mapsManager = MapsManager()
        mapsManager!!.initializePlacesAPI(requireContext())

        Database.setUpDatabase()
        initializeViews()
        setupListeners()
        setAdapters()
        updateUI()
        displayReviews()

        return binding.root
    }

    override fun onDestroyView() {
        reviewsRecyclerView.adapter = null

        valueEventListener.let {
            if (it != null) {
                Database.reviews.removeEventListener(it)
            }
        }
        mapsManager = null
        super.onDestroyView()
    }

    private fun displayReviews() {

        valueEventListener = Database.reviews.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reviewList = mutableListOf<Review>()

                for (reviewSnapshot in dataSnapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let { reviewList.add(it) }
                }

                reviewList.sortByDescending { it.timestamp }

                reviewAdapter = ReviewAdapter(reviewList,
                    requireContext(), mapsManager!!)
                reviewsRecyclerView.adapter = reviewAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT)
            }

        })
    }

    private fun initializeViews() {
        addButton = binding.addButton
        reviewsRecyclerView = binding.reviewsRecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        addButton.setOnClickListener { addButtonClicked() }
    }

    private fun setAdapters() {
        reviewAdapter = ReviewAdapter(emptyList(), requireContext(), mapsManager!!)
        reviewsRecyclerView.adapter = reviewAdapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun updateUI() {
        if (AuthManager.isGuestMode()){
            addButton.visibility = View.GONE
        } else{
            addButton.visibility = View.VISIBLE
        }
    }

    private fun addButtonClicked() {
        val intent = Intent(requireContext(), AddActivity::class.java)
        intent.putExtra("mode", "add")
        startActivity(intent)
    }


}
