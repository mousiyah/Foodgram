package com.example.foodgram.menu.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    private lateinit var speechToTextButton: ImageButton
    private lateinit var searchField: AutoCompleteTextView
    private lateinit var speechRecognition: ActivityResultLauncher<Intent>


    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    private var valueEventListener: ValueEventListener? = null
    private var mapsManager: MapsManager? = null

    private val speechRequestCode = 104
    private val recordAudioPermissionCode = 105

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        mapsManager = MapsManager()
        mapsManager!!.initializePlacesAPI(requireContext())

        Database.setUpDatabase()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        setupListeners()
        setupLaunchers()
        setAdapters()
        updateUI()
        displayReviews()

        speechToTextButton = view.findViewById(R.id.speechToTextButton)
        speechToTextButton.setOnClickListener { startSpeechToText() }
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

    private fun setupLaunchers() {
        speechRecognition = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results?.get(0) // Get the recognized speech text

                // Set the recognized text to the search field or perform any action
                searchField.setText(spokenText)
            }
        }
    }

    private fun startSpeechToText() {
        if(isMicrophonePermissionGranted()) {
            val context = requireContext()
            if (isSpeechRecognitionAvailable()) {
                val speechRecognizer = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                speechRecognizer.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                speechRecognizer.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")

                speechRecognition.launch(speechRecognizer)
            } else {
                Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestMicrophonePermission()
        }
    }

    private fun isSpeechRecognitionAvailable(): Boolean {
        val pm = requireContext().packageManager
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo != null
    }



    private fun isMicrophonePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestMicrophonePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            // Show an explanation to the user as to why the permission is needed
            Toast.makeText(
                requireContext(),
                "Microphone permission is required for speech recognition",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Request the permission using the launcher
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startSpeechToText()
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(
                    requireContext(),
                    "Microphone permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



}
