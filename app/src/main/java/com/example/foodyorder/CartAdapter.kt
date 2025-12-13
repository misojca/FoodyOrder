package com.example.foodyorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.lang.ref.WeakReference

class CartAdapter(
    private val cartList: MutableList<Cart>,
    // Prihvata Repository kao zavisnost
    private val cartRepository: CartRepository
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val dishName: TextView = itemView.findViewById(R.id.cart_item_name)
        val quantity: TextView = itemView.findViewById(R.id.cart_item_quantity)
        val price: TextView = itemView.findViewById(R.id.cart_item_price)

        val btnPlus: MaterialButton = itemView.findViewById(R.id.btn_plus)
        val btnMinus: MaterialButton = itemView.findViewById(R.id.btn_minus)
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
        holder.price.text = String.format("%.2f e", cartItem.price)

        holder.btnMinus.isEnabled = cartItem.quantity > 1

        if (cartItem.documentId.isEmpty()) {
            Toast.makeText(holder.itemView.context, "Missing ID of the item", Toast.LENGTH_LONG).show()
            holder.btnPlus.isEnabled = false
            holder.btnMinus.isEnabled = false
            return
        }

        val currentQuantity = cartItem.quantity.toDouble()
        val unitPrice = if (currentQuantity > 0) cartItem.price / currentQuantity else 0.0

        // Adapter sada koristi Repository za sve operacije na podacima
        val callback = object : CartOperationCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(exception: Exception, message: String) {
                Toast.makeText(holder.itemView.context, "$message: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

        holder.btnPlus.setOnClickListener {
            val newQuantity = cartItem.quantity + 1
            cartRepository.updateCartItemQuantity(cartItem, newQuantity, unitPrice, callback)
        }

        holder.btnMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                val newQuantity = cartItem.quantity - 1
                cartRepository.updateCartItemQuantity(cartItem, newQuantity, unitPrice, callback)
            } else {
                cartRepository.deleteCartItem(cartItem, callback)
            }
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    fun updateItems(newItems: List<Cart>) {
        cartList.clear()
        cartList.addAll(newItems)
        notifyDataSetChanged()
    }
}