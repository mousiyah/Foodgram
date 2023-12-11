package com.example.foodgram

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.foodgram.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        setupNavigation()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {

                    if (backstackExists()) {
                        controller.popBackStack()
                    }
                    setupAppBar(R.layout.app_bar_base)

                }
                R.id.navigation_browse -> {
                    if (backstackExists()) {
                        controller.popBackStack()
                    }
                    supportActionBar?.hide()
                }
                R.id.navigation_profile -> {
                    if (backstackExists()) {
                        controller.popBackStack()
                    }
                    setupAppBar(R.layout.app_bar_base)

                }
            }
        }
    }

    private fun backstackExists(): Boolean {
        val fragmentManager = supportFragmentManager
        return fragmentManager.backStackEntryCount > 0
    }

}