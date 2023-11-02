package com.example.foodgram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R
import com.example.foodgram.ui.home.adapter.PostAdapter

class HomeFragment : Fragment() {
    private lateinit var postAdapter: PostAdapter

    // Sample data for posts
    private val samplePosts = listOf(
        PostModel("Post 1", "Description for Post 1", R.drawable.ic_home),
        PostModel("Post 2", "Description for Post 2", R.drawable.ic_home),
        // Add more sample posts here
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.postRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create and set the adapter
        postAdapter = PostAdapter(samplePosts)
        recyclerView.adapter = postAdapter

        return view
    }
}
