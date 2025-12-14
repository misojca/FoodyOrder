package com.example.foodyorder

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class RestaurantRepository {

    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")
    private val TAG = "RestaurantRepository"

    fun fetchAllRestaurants(callback: RestaurantCallback) {

        Log.d(TAG, "Fetching all restaurants...")

        restaurantsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                handleQuerySnapshot(querySnapshot, callback)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching restaurants", e)
                callback.onFailure(e)
            }
    }

    private fun handleQuerySnapshot(querySnapshot: QuerySnapshot, callback: RestaurantCallback) {
        val restaurantList = mutableListOf<Restaurant>()

        for (document in querySnapshot.documents) {
            try {
                // Koristimo .toObject() za automatsko mapiranje
                val restaurant = document.toObject(Restaurant::class.java)?.apply {
                    // Kritično: Postavljanje documentId-ja (UID restorana)
                    this.documentId = document.id
                }
                if (restaurant != null) {
                    restaurantList.add(restaurant)
                } else {
                    Log.w(TAG, "Skipping document ${document.id}: Failed to map to Restaurant object.")
                }
            } catch (e: Exception) {
                // Hvatamo greške pri mapiranju (npr. neispravni tipovi podataka)
                Log.e(TAG, "Mapping error for document ${document.id}: ${e.message}")
            }
        }

        Log.d(TAG, "Successfully loaded ${restaurantList.size} restaurants.")
        callback.onRestaurantsLoaded(restaurantList)
    }
}