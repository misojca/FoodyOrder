package com.example.foodyorder.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyorder.utils.OnRestaurantClickListener
import com.example.foodyorder.R
import com.example.foodyorder.ui.adapter.RestaurantAdapter
import com.example.foodyorder.utils.RestaurantCallback
import com.example.foodyorder.data.model.Restaurant
import com.example.foodyorder.data.repository.RestaurantRepository
import com.example.foodyorder.ui.fragment.MenuFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    OnRestaurantClickListener,
    RestaurantCallback {

    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var restaurantsRecyclerView: RecyclerView
    private lateinit var fragmentContainer: FrameLayout

    private lateinit var restaurantRepository: RestaurantRepository

    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        loadingIndicator = findViewById(R.id.loading_indicator)

        restaurantsRecyclerView = findViewById(R.id.restaurants_recyclerview)
        fragmentContainer = findViewById(R.id.fragment_container)

        setSupportActionBar(toolbar)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        restaurantRepository = RestaurantRepository()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        checkAndSaveFCMToken()

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

        setupBackStackListener()


        val restaurantId = intent.getStringExtra("restaurantId")
        val restaurantName = intent.getStringExtra("restaurantName")

        if (restaurantId != null && restaurantName != null) {
            onRestaurantClick(restaurantId, restaurantName)
        } else {
            setupRestaurantsList()
        }
    }

    private fun setupBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val backStackCount = supportFragmentManager.backStackEntryCount
            Log.d(TAG, "Back Stack Count changed: $backStackCount")

            if (backStackCount == 0) {
                restaurantsRecyclerView.visibility = View.VISIBLE
                fragmentContainer.visibility = View.GONE
            } else {
                restaurantsRecyclerView.visibility = View.GONE
                fragmentContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun checkAndSaveFCMToken() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User ID is null, cannot save FCM token.")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM Token successfully saved/updated for user: $userId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating FCM Token for user: $userId", e)
                }
        }
    }


    override fun onRestaurantClick(restaurantId: String, restaurantName: String) {

        val menuFragment = MenuFragment.Companion.newInstance(restaurantId, restaurantName)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, menuFragment)
            .addToBackStack(null)
            .commit()

        restaurantsRecyclerView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
            }
            R.id.nav_logout -> {
                performLogout()
            }
            R.id.nav_maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
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
        restaurantsRecyclerView.layoutManager = LinearLayoutManager(this)
        loadingIndicator.visibility = View.VISIBLE
        restaurantRepository.fetchAllRestaurants(this)
    }

    override fun onRestaurantsLoaded(restaurants: List<Restaurant>) {
        restaurantsRecyclerView.adapter = RestaurantAdapter(restaurants, this)
        loadingIndicator.visibility = View.GONE
    }

    override fun onFailure(e: Exception) {
        Toast.makeText(this, "Error loading restaurants: ${e.message}", Toast.LENGTH_LONG).show()
        loadingIndicator.visibility = View.GONE
    }
}