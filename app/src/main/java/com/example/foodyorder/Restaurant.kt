package com.example.foodyorder

data class Restaurant(
    var documentId: String = "",
    val name: String = "",
    val cuisine: String = "",
    val rating: Float = 0.0f,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val openHours: Map<String, String> = emptyMap()) {

}