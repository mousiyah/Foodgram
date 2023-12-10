package com.example.foodgram.menu.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.Database
import com.example.foodgram.AuthManager
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentProfileBinding
import com.example.foodgram.login.LoginActivity
import com.example.foodgram.review.Review
import com.example.foodgram.review.ReviewAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView

    private lateinit var reviewsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        setupListeners()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initializeViews() {
        signInButton = binding.signInButton
        signOutButton = binding.signOutButton

        usernameTextView = binding.usernameTextView
        emailTextView = binding.emailTextView

        reviewsRecyclerView = binding.reviewsRecyclerView
    }

    private fun setupListeners() {
        signInButton.setOnClickListener { onSignInButtonClicked() }
        signOutButton.setOnClickListener { onSignOutButtonClicked() }
    }

    private fun onSignInButtonClicked() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun onSignOutButtonClicked() {
        AuthManager.signOut()
        updateUI()
    }

    private fun updateUI() {
        if (AuthManager.isGuestMode()){
            switchToGuestMode()
        } else{
            switchToProfileMode()
        }
    }

    private fun switchToGuestMode() {
        binding.signInLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    private fun switchToProfileMode() {
        binding.signInLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE

        displayGreeting()
        displayReviews()
    }

    private fun displayGreeting() {
        var greeting = getString(R.string.greeting) + " " +
                AuthManager.getUsername()
        usernameTextView.text = greeting

        emailTextView.text = AuthManager.getCurrentUser()?.email
    }

    private fun displayReviews() {

        // Query the database to fetch posts for the current user
        val query = Database.reviews.orderByChild("username").equalTo(AuthManager.getUsername())

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reviewList = mutableListOf<Review>()

                for (reviewSnapshot in dataSnapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let { reviewList.add(it) }
                }

                // Set up adapter
                var maps = MapsManager(requireContext())
                val adapter = ReviewAdapter(reviewList,
                                            requireContext(),
                                            maps.placesClient)
                reviewsRecyclerView.adapter = adapter
                reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT)
            }

        })
    }


}