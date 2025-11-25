package com.example.foodyorder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnRestaurantClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var loadingIndicator: ProgressBar
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        loadingIndicator = findViewById(R.id.loading_indicator)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)


        setupRestaurantsList()
    }

    override fun onRestaurantClick(restaurantId: String, restaurantName: String) {
        val menuFragment = MenuFragment.newInstance(restaurantId, restaurantName)


        supportFragmentManager.beginTransaction()

            .replace(R.id.fragment_container, menuFragment)
            .addToBackStack(null)
            .commit()

        findViewById<RecyclerView>(R.id.restaurants_recyclerview).visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_cart -> {
                Toast.makeText(this, "Opening My Cart", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CartActivity::class.java))
                finish()
            }
            R.id.nav_logout -> {
                performLogout()
            }
            R.id.nav_maps -> {
                startActivity(Intent(this, MapsActivity   ::class.java))
            }
        }
        drawerLayout.closeDrawers()
        return true
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupRestaurantsList() {

        val recyclerView: RecyclerView = findViewById(R.id.restaurants_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadingIndicator.visibility = View.VISIBLE

        db.collection("restaurants")
            .get()
            .addOnSuccessListener { result ->
                val restaurantList = mutableListOf<Restaurant>()
                for (document in result) {

                    val restaurant = document.toObject(Restaurant::class.java)
                    restaurant.documentId = document.id
                    restaurantList.add(restaurant)
                }

                recyclerView.adapter = RestaurantAdapter(restaurantList,this)
                loadingIndicator.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading reastaurants: ${exception.message}", Toast.LENGTH_LONG).show()
                loadingIndicator.visibility = View.GONE
                exception.printStackTrace()
            }

    }


}