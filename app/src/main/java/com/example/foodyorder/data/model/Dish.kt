package com.example.foodyorder.data.model

data class Dish ( var documentId: String = "",var dishName: String = "",
                  var dishDesc: String = "",var dishPrice: Double = 0.0,

                  var url: String = ""){

    constructor() : this("", "", "",0.0,"")

}