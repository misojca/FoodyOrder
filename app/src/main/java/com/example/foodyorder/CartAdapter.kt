/*package com.example.foodyorder

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
*

 */
package com.example.foodyorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class CartAdapter(
    private val cartList: MutableList<Cart>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

        val userId = auth.currentUser?.uid
        if (userId == null) {
            holder.btnPlus.isEnabled = false
            holder.btnMinus.isEnabled = false
            Toast.makeText(holder.itemView.context, "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (cartItem.documentId.isEmpty()) {
            Toast.makeText(holder.itemView.context, "Missing ID of the item", Toast.LENGTH_LONG).show()
            holder.btnPlus.isEnabled = false
            holder.btnMinus.isEnabled = false
            return
        }

        val currentQuantity = cartItem.quantity.toDouble()
        val unitPrice = if (currentQuantity > 0) cartItem.price / currentQuantity else 0.0

        val itemRef = db.collection("users")
            .document(userId)
            .collection("cart")
            .document(cartItem.documentId)

        holder.btnPlus.setOnClickListener {
            val newQuantity = cartItem.quantity + 1

            val newTotalPrice = unitPrice * newQuantity.toDouble()

            itemRef.update(
                "quantity", newQuantity,
                "price", newTotalPrice
            ).addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "The amount of dish ${cartItem.name} has increased", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Increasing error ${it.message}", Toast.LENGTH_LONG).show()
            }
        }


        holder.btnMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                val newQuantity = cartItem.quantity - 1

                val newTotalPrice = unitPrice * newQuantity.toDouble()

                itemRef.update(
                    "quantity", newQuantity,
                    "price", newTotalPrice
                ).addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "The amount of dish${cartItem.name} has decreased", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "Decreasing error ${it.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                itemRef.delete().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Dish ${cartItem.name} removed from the cart", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "Deleting error: ${it.message}", Toast.LENGTH_LONG).show()
                }
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