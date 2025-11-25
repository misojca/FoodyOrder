package com.example.foodyorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableUserLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as com.google.android.gms.maps.SupportMapFragment
        mapFragment.getMapAsync(this)
    }
/*
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val db = FirebaseFirestore.getInstance()

        db.collection("restaurants")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val restaurant = document.toObject(Restaurant::class.java)
                    restaurant.documentId = document.id

                    val position = LatLng(restaurant.lat, restaurant.lng)

                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(restaurant.name)
                    )

                    marker?.tag = restaurant
                }
            }

        mMap.setOnMarkerClickListener { marker ->

            val restaurant = marker.tag as? Restaurant

            if (restaurant != null) {

                val today = getTodayName()
                val hoursToday = restaurant.openHours[today] ?: "No info"

                val message = """
                ${restaurant.name}
                Today: $hoursToday
                Rating: ${restaurant.rating}
            """.trimIndent()

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("restaurantId", restaurant.documentId)
                intent.putExtra("restaurantName", restaurant.name)
                startActivity(intent)
            }

            true
        }
    }


*/
override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    requestLocationPermission()

    // postavi adapter za InfoWindow
    mMap.setInfoWindowAdapter(RestaurantInfoWindowAdapter(this))

    // load restorana
    loadRestaurants()

    // klik na InfoWindow
    mMap.setOnInfoWindowClickListener { marker ->
        val restaurant = marker.tag as? Restaurant ?: return@setOnInfoWindowClickListener

        // Pokreće HomeActivity i otvara restoran
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("restaurantId", restaurant.documentId)
        intent.putExtra("restaurantName", restaurant.name)
        startActivity(intent)
    }
}

    private fun loadRestaurants() {
        firestore.collection("restaurants").get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                val restaurant = doc.toObject(Restaurant::class.java) ?: continue
                restaurant.documentId = doc.id  // <- ovo fali

                val position = LatLng(restaurant.lat, restaurant.lng)
                val marker = mMap.addMarker(
                    MarkerOptions().position(position).title(restaurant.name)
                )
                marker?.tag = restaurant
            }
        }
    }


    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> enableUserLocation()

            else -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation() {
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
            }
        }
    }



    private fun getTodayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "mon"
            Calendar.TUESDAY -> "tue"
            Calendar.WEDNESDAY -> "wed"
            Calendar.THURSDAY -> "thu"
            Calendar.FRIDAY -> "fri"
            Calendar.SATURDAY -> "sat"
            else -> "sun"
        }
    }

}
