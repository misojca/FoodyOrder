package com.example.foodyorder


data class Dish ( var documentId: String = "",var dishName: String = "",
                  var dishDesc: String = "",var dishPrice: Double = 0.0, var dishImageURL: String){

    constructor() : this("", "", "",0.0,"")

}