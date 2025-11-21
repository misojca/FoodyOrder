package com.example.foodyorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(private val cartList:List<Cart>) : RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val dishName: TextView = itemView.findViewById(R.id.cart_item_name)
        val quantity: TextView = itemView.findViewById(R.id.cart_item_quantity)
        val price: TextView = itemView.findViewById(R.id.cart_item_price)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartAdapter.CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartAdapter.CartViewHolder, position: Int) {
        val cartItem = cartList[position]
        holder.dishName.text = cartItem.name
        holder.quantity.text = "x${cartItem.quantity}"
        holder.price.text = "${cartItem.price} e"
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

}