package com.example.foodgram.menu.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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

    private var mapsManager: MapsManager? = null

    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView

    private lateinit var reviewsRecyclerView: RecyclerView

    private lateinit var myReviewsTab: LinearLayout
    private lateinit var savedTab: LinearLayout

    private var myReviewsEventListener: ValueEventListener? = null
    private var savedReviewsEventListener: ValueEventListener? = null

    private var selectedTabId: Int = R.id.myReviewsTab


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        mapsManager = MapsManager()
        mapsManager!!.initializePlacesAPI(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt("selectedTabId")
            restoreSelectedTab()
        } else {
            switchToMyReviewsTab()
        }

        initializeViews()
        setupListeners()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selectedTabId", selectedTabId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        removeListeners()
        reviewsRecyclerView.adapter = null
        mapsManager = null
        super.onDestroyView()
    }

    private fun restoreSelectedTab() {
        when (selectedTabId) {
            R.id.myReviewsTab -> switchToMyReviewsTab()
            R.id.savedTab -> switchToSavedTab()
        }
    }

    private fun removeListeners() {

        myReviewsEventListener.let {
            if (it != null) {
                Database.myReviewsQuery.removeEventListener(it)
            }
        }
        myReviewsEventListener = null
        savedReviewsEventListener.let {
            if (it != null) {
                Database.savedReviews!!.removeEventListener(it)
            }
        }
        savedReviewsEventListener = null
    }


    private fun initializeViews() {
        signInButton = binding.signInButton
        signOutButton = binding.signOutButton

        usernameTextView = binding.usernameTextView
        emailTextView = binding.emailTextView

        reviewsRecyclerView = binding.reviewsRecyclerView

        myReviewsTab = binding.myReviewsTab
        savedTab = binding.savedTab
    }

    private fun setupListeners() {
        signInButton.setOnClickListener { onSignInButtonClicked() }
        signOutButton.setOnClickListener { onSignOutButtonClicked() }
        myReviewsTab.setOnClickListener({ switchToMyReviewsTab() })
        savedTab.setOnClickListener({ switchToSavedTab() })
    }

    private fun onSignInButtonClicked() {
        val sharedPreferences =
            requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isGuestModeOn", false)
        editor.apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun onSignOutButtonClicked() {
        AuthManager.signOut()
        updateUI()
    }

    private fun switchToMyReviewsTab() {
        binding.myReviewsLine.setBackgroundColor(
            resources.getColor(
                R.color.purple,
                requireContext().theme
            )
        )
        binding.savedLine.setBackgroundColor(
            resources.getColor(
                R.color.white,
                requireContext().theme
            )
        )
        displayMyReviews()

        selectedTabId = R.id.myReviewsTab
    }

    private fun switchToSavedTab() {
        binding.savedLine.setBackgroundColor(
            resources.getColor(
                R.color.purple,
                requireContext().theme
            )
        )
        binding.myReviewsLine.setBackgroundColor(
            resources.getColor(
                R.color.white,
                requireContext().theme
            )
        )
        displaySavedReviews()

        selectedTabId = R.id.savedTab
    }

    private fun updateUI() {
        if (AuthManager.isGuestMode()) {
            switchToGuestMode()
        } else {
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
    }

    private fun displayGreeting() {
        var greeting = getString(R.string.greeting) + " " +
                AuthManager.getUsername()
        usernameTextView.text = greeting

        emailTextView.text = AuthManager.getCurrentUser()?.email
    }

    private fun displayMyReviews() {
        myReviewsEventListener =
            Database.myReviewsQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isAdded) {
                        val reviewList = mutableListOf<Review>()
                        var hasContent = false

                        for (reviewSnapshot in dataSnapshot.children) {
                            val review = reviewSnapshot.getValue(Review::class.java)
                            review?.let { reviewList.add(it) }
                        }

                        if (reviewList.isNotEmpty()) {
                            hasContent = true
                            reviewList.sortByDescending { it.timestamp }
                            updateRecycleView(reviewList)
                        }

                        if (hasContent) {
                            showContent()
                        } else {
                            noContent()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun displaySavedReviews() {
        Database.savedReviews!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (isAdded) {
                    val savedReviewIDs = mutableListOf<String>()

                    for (reviewSnapshot in dataSnapshot.children) {
                        val reviewID = reviewSnapshot.value as String
                        savedReviewIDs.add(reviewID)
                    }

                    val savedReviewList = mutableListOf<Review>()
                    val totalReviews = savedReviewIDs.size
                    var reviewsProcessed = 0

                    if (totalReviews == 0) {
                        noContent()
                    } else {
                        savedReviewIDs.forEach { reviewID ->
                            Database.getReviewByID(reviewID) { review ->
                                review?.let { savedReviewList.add(it) }

                                reviewsProcessed++
                                if (reviewsProcessed == totalReviews) {
                                    if (savedReviewList.isEmpty()) {
                                        noContent()
                                    } else {
                                        showContent()

                                        savedReviewList.sortByDescending { it.timestamp }
                                        updateRecycleView(savedReviewList)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun noContent() {
        binding.noContent.visibility = View.VISIBLE
        reviewsRecyclerView.visibility = View.GONE
    }

    private fun showContent() {
        binding.noContent.visibility = View.GONE
        reviewsRecyclerView.visibility = View.VISIBLE
    }

    private fun updateRecycleView(reviewList: MutableList<Review>) {
        val adapter = ReviewAdapter(reviewList,
            requireContext(), mapsManager!!)
        reviewsRecyclerView.adapter = adapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


}