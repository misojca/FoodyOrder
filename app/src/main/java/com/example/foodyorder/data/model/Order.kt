package com.example.foodyorder.data.model

data class Order(
    val restaurantName: String = "",
    val dishes: List<Map<String, Any>> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = ""
)