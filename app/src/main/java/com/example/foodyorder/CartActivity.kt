package com.example.foodyorder

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth;
    private lateinit var cartRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        loadCartItems()
    }

    private fun loadCartItems(){
        var userId = auth.currentUser?.uid

        if(userId == null){
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                var cartList = mutableListOf<Cart>()

                for (document in result){
                    val item = document.toObject(Cart::class.java)
                    cartList.add(item)
                }

                cartRecyclerView.adapter = CartAdapter(cartList)
                Toast.makeText(this, "Loaded ${cartList.size} items", Toast.LENGTH_SHORT).show()
                Log.d("CartActivity", "Items loaded: ${cartList.size}")

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CartActivity", "Error loading cart", e)
            }
    }
}