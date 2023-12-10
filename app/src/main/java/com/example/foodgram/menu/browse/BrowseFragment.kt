package com.example.foodgram.menu.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgram.R
import com.example.foodgram.databinding.FragmentBrowseBinding

class BrowseFragment : Fragment() {

    private lateinit var binding: FragmentBrowseBinding
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowseBinding.inflate(inflater, container, false)
        root = binding.root

        setupRecyclerView()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setupRecyclerView(){
        val recyclerView: RecyclerView = root.findViewById(R.id.categories_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 cards per row

        val data = listOf(
            CategoryAdapter.CategoryItem("Fast Food", R.drawable.c1),
            CategoryAdapter.CategoryItem("Breakfast and Brunch", R.drawable.c2),
            CategoryAdapter.CategoryItem("Coffe and Tea", R.drawable.c3),
            CategoryAdapter.CategoryItem("Pizza", R.drawable.c4),
            CategoryAdapter.CategoryItem("Desserts", R.drawable.c5),
            CategoryAdapter.CategoryItem("Indian", R.drawable.c6),
            CategoryAdapter.CategoryItem("Chinese", R.drawable.c7),
            CategoryAdapter.CategoryItem("Vegan", R.drawable.c8)
        )

        val adapter = CategoryAdapter(data)
        recyclerView.adapter = adapter
    }
}