package com.example.foodgram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R

class HomeFragment : Fragment() {

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewList: List<ReviewItem>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        reviewsRecyclerView = root.findViewById(R.id.reviewsRecyclerView)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        populateReviews()
            reviewAdapter = ReviewAdapter(requireContext(), reviewList)
        reviewsRecyclerView.adapter = reviewAdapter

        return root
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



    override fun onDestroyView() {
        super.onDestroyView()
    }
}
