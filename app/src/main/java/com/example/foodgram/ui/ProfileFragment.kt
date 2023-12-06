package com.example.foodgram.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodgram.AuthManager
import com.example.foodgram.LoginActivity
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var usernameTextView: TextView

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
    }

    private fun setupListeners() {
        signInButton.setOnClickListener { onSignInButtonClicked() }
        signOutButton.setOnClickListener { onSignOutButtonClicked() }
    }

    private fun updateUI() {
        if (AuthManager.isGuestMode()){
            switchToGuestProfile()
        } else{
            switchToCurrentProfile()
        }
    }

    private fun switchToGuestProfile() {
        binding.signInLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    private fun switchToCurrentProfile() {
        binding.signInLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE

        var greeting = getString(R.string.greeting) + " " +
                AuthManager.getUsername()
        usernameTextView.text = greeting
    }

    private fun onSignInButtonClicked() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun onSignOutButtonClicked() {
        AuthManager.signOut()
        updateUI()
    }
}