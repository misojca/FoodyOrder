package com.example.foodyorder.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyorder.R
import com.example.foodyorder.data.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(private val orderList: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName = itemView.findViewById<TextView>(R.id.tvRestaurantName)
        val dishes = itemView.findViewById<TextView>(R.id.tvDishes)
        val price = itemView.findViewById<TextView>(R.id.tvPrice)
        val date = itemView.findViewById<TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.restaurantName.text = order.restaurantName
        holder.price.text = "Total: ${order.totalPrice} e"

        val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(order.timestamp))
        holder.date.text = "Date: $formattedDate"

        val dishesText = order.dishes.joinToString("\n") {
            "• ${it["name"]} x${it["quantity"]} - ${it["price"]} e"
        }

        holder.dishes.text = dishesText
    }

    override fun getItemCount(): Int = orderList.size
}