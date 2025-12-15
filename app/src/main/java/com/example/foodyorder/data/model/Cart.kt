package com.example.foodyorder.data.model

import com.google.firebase.firestore.DocumentId

data class Cart(
    @DocumentId
    val documentId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val restaurantId: String = ""
) {
    @Suppress("unused")
    constructor() : this("", "", 0.0, 0, "", "")
}