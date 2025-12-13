package com.example.foodyorder

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