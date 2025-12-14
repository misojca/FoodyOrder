package com.example.foodyorder.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodyorder.ui.adapter.DishAdapter
import com.example.foodyorder.R
import com.example.foodyorder.data.model.Dish
import com.google.firebase.firestore.FirebaseFirestore


private const val ARG_RESTAURANT_ID = "restaurant_id"
private const val ARG_RESTAURANT_NAME = "restaurant_name"

class MenuFragment : Fragment() {

    private lateinit var dishesRecyclerView: RecyclerView
    private lateinit var restaurantNameTextView: TextView
    private lateinit var db: FirebaseFirestore
    private var restaurantId: String? = null
    private var restaurantName: String? = null

    private lateinit var dishAdapter: DishAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        arguments?.let {
            restaurantId = it.getString(ARG_RESTAURANT_ID)
            restaurantName = it.getString(ARG_RESTAURANT_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        dishesRecyclerView = view.findViewById(R.id.dishes_recyclerview)
        restaurantNameTextView = view.findViewById(R.id.menu_restaurant_name)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restaurantNameTextView.text = restaurantName ?: "Menu"


        dishesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        dishAdapter = DishAdapter(mutableListOf())
        dishesRecyclerView.adapter = dishAdapter



        restaurantId?.let { id ->

            loadDishes(id)
        } ?: run {
            Toast.makeText(requireContext(), "Restaurant ID not found.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadDishes(restaurantId: String) {



        db.collection("restaurants")
            .document(restaurantId)
            .collection("menu")
            .get()
            .addOnSuccessListener { result ->
                val dishList = mutableListOf<Dish>()
                for (document in result) {

                    val dish = document.toObject(Dish::class.java).apply {
                        this.documentId = document.id
                    }
                    dishList.add(dish)
                }




                dishAdapter.updateItems(dishList)
                Toast.makeText(requireContext(), "Učitano ${dishList.size} jela.", Toast.LENGTH_SHORT).show()

                Log.d("MenuFragment", "Učitano jela: ${dishList.size}")



            }
            .addOnFailureListener { exception ->

                Toast.makeText(requireContext(), "Greška pri učitavanju menija: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("MenuFragment", "Greška pri dohvatanju jela", exception)

            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *

         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(restaurantId: String, restaurantName: String) =
            MenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RESTAURANT_ID, restaurantId)
                    putString(ARG_RESTAURANT_NAME, restaurantName)
                }
            }
    }
}