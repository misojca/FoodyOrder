package com.example.foodyorder.utils

import com.example.foodyorder.data.model.Restaurant

interface OnRestaurantClickListener {
    fun onRestaurantClick(restaurantId: String, restaurantName: String)
}
interface CartOperationCallback {
    fun onSuccess(message: String)
    fun onFailure(exception: Exception, message: String)
}

interface RestaurantCallback {
    fun onRestaurantsLoaded(restaurants: List<Restaurant>)
    fun onFailure(e: Exception)
}