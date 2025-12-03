package com.example.foodyorder

data class Cart(
    var documentId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)
