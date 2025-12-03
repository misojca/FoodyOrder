/*package com.example.foodyorder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
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

        val restaurantId = intent.getStringExtra("restaurantId")
        val restaurantName = intent.getStringExtra("restaurantName")

        if (restaurantId != null && restaurantName != null) {
            onRestaurantClick(restaurantId, restaurantName)
        }

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
        onBackPressedDispatcher.addCallback(this) {
            val fragmentManager = supportFragmentManager

            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
            } else {
                moveTaskToBack(true)
            }
        }

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

                Log.d("FIREBASE_DEBUG", "Document count: ${result.size()}")

                val restaurantList = mutableListOf<Restaurant>()

                for (document in result) {

                    Log.d("FIREBASE_RAW_DOC", "Document ID: ${document.id}, Data: ${document.data}")

                    val openHoursData = document.get("openHours")

                    Log.d("FIREBASE_OPEN_HOURS_RAW", "Raw openHours value: $openHoursData")

                    val restaurant = Restaurant(
                        documentId = document.id,
                        name = document.getString("name") ?: "",
                        cuisine = document.getString("cuisine") ?: "",
                        rating = document.getDouble("rating")?.toFloat() ?: 0f,
                        lat = document.getDouble("lat") ?: 0.0,
                        lng = document.getDouble("lng") ?: 0.0,
                        openHours = document.get("openHours") as? Map<String, String> ?: emptyMap()
                    )

                    Log.d("PARSED_RESTAURANT", "Restaurant loaded: $restaurant")
                    Log.d("PARSED_OPEN_HOURS", "Parsed openHours: ${restaurant.openHours}")

                    restaurantList.add(restaurant)
                }

                recyclerView.adapter = RestaurantAdapter(restaurantList, this)
                loadingIndicator.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE_ERROR", "Error loading: ${exception.message}")
                Toast.makeText(this, "Error loading restaurants: ${exception.message}", Toast.LENGTH_LONG).show()
                loadingIndicator.visibility = View.GONE
            }
    }



}
*/
package com.example.foodyorder

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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnRestaurantClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var restaurantsRecyclerView: RecyclerView
    private lateinit var fragmentContainer: FrameLayout

    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        loadingIndicator = findViewById(R.id.loading_indicator)


        restaurantsRecyclerView = findViewById(R.id.restaurants_recyclerview)
        fragmentContainer = findViewById(R.id.fragment_container)


        setSupportActionBar(toolbar)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
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

        // Asinhrono dohvatanje tokena
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            // Ažuriranje polja 'fcmToken' u dokumentu korisnika
            db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM Token successfully saved/updated for user: $userId")
                }
                .addOnFailureListener { e ->
                    // Ovo se može desiti ako polje 'fcmToken' još uvek ne postoji u dokumentu
                    Log.e(TAG, "Error updating FCM Token for user: $userId", e)
                }
        }
    }


    override fun onRestaurantClick(restaurantId: String, restaurantName: String) {

        val menuFragment = MenuFragment.newInstance(restaurantId, restaurantName)


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

        val recyclerView: RecyclerView = findViewById(R.id.restaurants_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadingIndicator.visibility = View.VISIBLE

        db.collection("restaurants")
            .get()
            .addOnSuccessListener { result ->

                Log.d("FIREBASE_DEBUG", "Document count: ${result.size()}")

                val restaurantList = mutableListOf<Restaurant>()

                for (document in result) {
                    val restaurant = Restaurant(
                        documentId = document.id,
                        name = document.getString("name") ?: "",
                        cuisine = document.getString("cuisine") ?: "",
                        rating = document.getDouble("rating")?.toFloat() ?: 0f,
                        lat = document.getDouble("lat") ?: 0.0,
                        lng = document.getDouble("lng") ?: 0.0,
                        openHours = document.get("openHours") as? Map<String, String> ?: emptyMap()
                    )

                    Log.d("PARSED_RESTAURANT", "Restaurant loaded: $restaurant")
                    restaurantList.add(restaurant)
                }

                recyclerView.adapter = RestaurantAdapter(restaurantList, this)
                loadingIndicator.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE_ERROR", "Error loading: ${exception.message}")
                Toast.makeText(this, "Error loading restaurants: ${exception.message}", Toast.LENGTH_LONG).show()
                loadingIndicator.visibility = View.GONE
            }
    }
}