package com.example.foodyorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface OnRestaurantClickListener {
    fun onRestaurantClick(restaurantId: String, restaurantName: String)
}
class RestaurantAdapter(private val restaurantList: List<Restaurant>,private val clickListener: OnRestaurantClickListener) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView: TextView = itemView.findViewById(R.id.restaurant_name_text)
        val cuisineTextView: TextView = itemView.findViewById(R.id.restaurant_cuisine_text)
        val ratingTextView: TextView = itemView.findViewById(R.id.restaurant_rating_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant,parent,false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {

        val restaurant = restaurantList[position]
        holder.nameTextView.text = restaurant.name
        holder.cuisineTextView.text = restaurant.cuisine
        holder.ratingTextView.text = "⭐ ${restaurant.rating}"

        holder.itemView.setOnClickListener {

            clickListener.onRestaurantClick(restaurant.documentId, restaurant.name)
        }
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }



}