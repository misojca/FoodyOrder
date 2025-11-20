package com.example.foodyorder


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class DishAdapter(private val dishList: MutableList<Dish>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameTextView: TextView = itemView.findViewById(R.id.dish_name_text)
        val descriptionTextView: TextView = itemView.findViewById(R.id.dish_description_text)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]


        holder.nameTextView.text = dish.dishName


        holder.descriptionTextView.text = dish.dishDesc


        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Click on  ${dish.dishName}", Toast.LENGTH_SHORT).show()
            // Logika za dodavanje u korpu ide ovde
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