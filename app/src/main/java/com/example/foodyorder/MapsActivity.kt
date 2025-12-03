package com.example.foodyorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
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
    private var backPressedTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as com.google.android.gms.maps.SupportMapFragment
        mapFragment.getMapAsync(this)

        onBackPressedDispatcher.addCallback(this) {

            if (backPressedTime + 2000 > System.currentTimeMillis()) return@addCallback

            backPressedTime = System.currentTimeMillis()

            val intent = Intent(this@MapsActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    requestLocationPermission()

    mMap.setInfoWindowAdapter(RestaurantInfoWindowAdapter(this))

    loadRestaurants()


    mMap.setOnInfoWindowClickListener { marker ->
        val restaurant = marker.tag as? Restaurant ?: return@setOnInfoWindowClickListener


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
                restaurant.documentId = doc.id

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


}
