package com.example.foodyorder

data class Dish ( var documentId: String = "",var dishName: String = "",
                  var dishDesc: String = "",){

    constructor() : this("", "", "")

}