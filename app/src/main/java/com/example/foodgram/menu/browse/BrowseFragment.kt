package com.example.foodgram.menu.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.Database
import com.example.foodgram.MapsManager
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentBrowseBinding
import com.example.foodgram.review.Review
import com.example.foodgram.review.ReviewAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BrowseFragment : Fragment(), CategoryAdapter.OnItemClickListener{

    private lateinit var binding: FragmentBrowseBinding
    private lateinit var root: View

    private lateinit var reviewsRecyclerView: RecyclerView
    private var mapsManager: MapsManager? = null
    private var reviewsEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowseBinding.inflate(inflater, container, false)
        root = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapsManager = MapsManager()
        mapsManager!!.initializePlacesAPI(requireContext())

        setupViews()
    }

    override fun onDestroyView() {

        reviewsEventListener.let {
            if (it != null) {
                //Database remove event listener
            }
        }

        reviewsRecyclerView.adapter = null
        mapsManager = null

        super.onDestroyView()
    }

    private fun setupViews() {

        reviewsRecyclerView = binding.reviewsRecyclerView
        setupCategories()

        binding.backButton.setOnClickListener{showCategoriesLayout()}
    }

    private fun setupCategories(){
        val recyclerView: RecyclerView = root.findViewById(R.id.categories_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 cards per row

        val data = listOf(
            CategoryAdapter.CategoryItem("Fast Food", R.drawable.c1),
            CategoryAdapter.CategoryItem("Breakfast and Brunch", R.drawable.c2),
            CategoryAdapter.CategoryItem("Coffee and Tea", R.drawable.c3),
            CategoryAdapter.CategoryItem("Pizza", R.drawable.c4),
            CategoryAdapter.CategoryItem("Dessert", R.drawable.c5),
            CategoryAdapter.CategoryItem("Indian", R.drawable.c6),
            CategoryAdapter.CategoryItem("Noodle", R.drawable.c7),
            CategoryAdapter.CategoryItem("Vegan", R.drawable.c8)
        )

        val adapter = CategoryAdapter(data, this)
        recyclerView.adapter = adapter
    }

    override fun onCategoryItemClick(categoryItem: CategoryAdapter.CategoryItem) {
        showReviewsLayout()
        fetchReviews(categoryItem.text)
        binding.categoryTextView.text = categoryItem.text
    }

    private fun fetchReviews(categoryName: String) {
        reviewsEventListener =
            Database.reviews.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isAdded) {
                        val reviewList = mutableListOf<Review>()
                        var hasContent = false

                        for (reviewSnapshot in dataSnapshot.children) {
                            val review = reviewSnapshot.getValue(Review::class.java)

                            if (review != null) {
                                if (review.foodName == categoryName) {
                                    review.let { reviewList.add(it) }
                                }
                            }
                        }

                        if (reviewList.isNotEmpty()) {
                            hasContent = true
                            reviewList.sortByDescending { it.timestamp }
                            updateRecycleView(reviewList)
                        }

                        if (hasContent) {
                            showReviews()
                        } else {
                            noReviews()
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

    private fun updateRecycleView(reviewList: MutableList<Review>) {
        val adapter = ReviewAdapter(reviewList,
            requireContext(), mapsManager!!)
        reviewsRecyclerView.adapter = adapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }



    private fun noReviews() {
        reviewsRecyclerView.visibility = View.GONE
        binding.noContent.visibility = View.VISIBLE
    }

    private fun showReviews() {
        reviewsRecyclerView.visibility = View.VISIBLE
        binding.noContent.visibility = View.GONE
    }

    private fun showReviewsLayout() {
        binding.reviews.visibility = View.VISIBLE
        binding.categories.visibility = View.GONE
        binding.searchBarBrowse.visibility = View.GONE
    }

    private fun showCategoriesLayout() {
        binding.reviews.visibility = View.GONE
        binding.categories.visibility = View.VISIBLE
        binding.searchBarBrowse.visibility = View.VISIBLE
    }


}