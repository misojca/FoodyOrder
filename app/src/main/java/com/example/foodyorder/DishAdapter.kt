package com.example.foodyorder


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DishAdapter(private val dishList: MutableList<Dish>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameTextView: TextView = itemView.findViewById(R.id.dish_name_text)
        val descriptionTextView: TextView = itemView.findViewById(R.id.dish_description_text)
        val dishImageView: ImageView = itemView.findViewById(R.id.dish_image)

        val buyButton: Button = itemView.findViewById(R.id.dish_button)
        val priceTextView: TextView = itemView.findViewById(R.id.dish_price_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]

        Glide.with(holder.itemView.context)
            .load(dish.dishImageURL)
            .into(holder.dishImageView)
        holder.nameTextView.text = dish.dishName


        holder.descriptionTextView.text = dish.dishDesc
        holder.priceTextView.text = dish.dishPrice.toString()

        holder.buyButton.setOnClickListener {

            val userId = auth.currentUser?.uid
            val dishUnitPrice = dish.dishPrice.toDouble()

            if (userId == null) {
                Toast.makeText(holder.itemView.context, "You must be logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cartRef = db.collection("users")
                .document(userId)
                .collection("cart")
                .document(dish.documentId)

            cartRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val currentQty = doc.getLong("quantity") ?: 0
                    val newQuantity = currentQty + 1
                    val newTotalPrice = dishUnitPrice * newQuantity

                    cartRef.update(
                        "quantity", newQuantity,
                        "price", newTotalPrice // Ažuriranje UKUPNE cene u korpi
                    )
                    Toast.makeText(holder.itemView.context, "${dish.dishName} added in cart", Toast.LENGTH_SHORT).show()
                } else {
                    //OVDE DODATI
                    val cartItem = mapOf(
                        "name" to dish.dishName,
                        "quantity" to 1,
                        "price" to dishUnitPrice
                    )

                    cartRef.set(cartItem)
                    Toast.makeText(holder.itemView.context, "${dish.dishName} added in cart", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return dishList.size
    }

    fun updateItems(newItems: List<Dish>) {
        dishList.clear()
        dishList.addAll(newItems)
        notifyDataSetChanged()
    }
}