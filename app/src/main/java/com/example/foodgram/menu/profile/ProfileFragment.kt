package com.example.foodgram.menu.profile

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.RawContacts.Data
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
import java.util.EventListener

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

        initializeViews()
        setupListeners()
        updateUI()
    }

    override fun onDestroyView() {
        removeListeners()
        reviewsRecyclerView.adapter = null
        mapsManager = null
        super.onDestroyView()
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
                Database.savedReviews?.removeEventListener(it)
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
        myReviewsTab.setOnClickListener( {switchToMyReviewsTab()} )
        savedTab.setOnClickListener( {switchToSavedTab()} )
    }

    private fun onSignInButtonClicked() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun onSignOutButtonClicked() {
        AuthManager.signOut()
        updateUI()
    }

    private fun switchToMyReviewsTab() {
        binding.myReviewsLine.setBackgroundColor(resources.getColor(R.color.purple, requireContext().theme))
        binding.savedLine.setBackgroundColor(resources.getColor(R.color.dark_grey, requireContext().theme))
        displayMyReviews()
    }

    private fun switchToSavedTab() {
        binding.savedLine.setBackgroundColor(resources.getColor(R.color.purple, requireContext().theme))
        binding.myReviewsLine.setBackgroundColor(resources.getColor(R.color.dark_grey, requireContext().theme))
        displaySavedReviews()
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
        switchToMyReviewsTab()
    }

    private fun displayGreeting() {
        var greeting = getString(R.string.greeting) + " " +
                AuthManager.getUsername()
        usernameTextView.text = greeting

        emailTextView.text = AuthManager.getCurrentUser()?.email
    }

    private fun displayMyReviews() {

        myReviewsEventListener = Database.myReviewsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reviewList = mutableListOf<Review>()

                for (reviewSnapshot in dataSnapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let { reviewList.add(it) }
                }

                if (reviewList.isEmpty()){
                    binding.noContent.visibility = View.VISIBLE
                    reviewsRecyclerView.visibility = View.GONE
                } else {
                    binding.noContent.visibility = View.GONE
                    reviewsRecyclerView.visibility = View.VISIBLE


                    reviewList.sortByDescending { it.timestamp }
                    updateRecycleView(reviewList)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT)
            }

        })
    }

    private fun displaySavedReviews() {
        Database.savedReviews!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val savedReviewList = mutableListOf<Review>()

                for (reviewSnapshot in dataSnapshot.children) {
                    val reviewID = reviewSnapshot.value as String

                    Database.getReviewByID(reviewID) { review ->
                        review?.let { savedReviewList.add(it) }

                    }
                }

                savedReviewList.reverse()
                updateRecycleView(savedReviewList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT)
            }
        })
    }


    private fun updateRecycleView(reviewList: MutableList<Review>) {
        val adapter = ReviewAdapter(reviewList,
            requireContext(), mapsManager!!)
        reviewsRecyclerView.adapter = adapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


}