package com.example.foodyorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback

class CartActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth;
    private var cartList = mutableListOf<Cart>()
    private lateinit var cartRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        val btnOrder: Button = findViewById(R.id.btn_order)

        loadCartItems()

        btnOrder.setOnClickListener {
            placeOrder(cartList)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })


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
               // var cartList = mutableListOf<Cart>()
                cartList.clear()
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

    private fun placeOrder(cartList: List<Cart>){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val totalPrice = cartList.sumOf { it.price * it.quantity }

        val orderData = hashMapOf(
            "userId" to userId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis(),
            "totalPrice" to totalPrice,
            "dishes" to cartList.map {
                hashMapOf(
                    "name" to it.name,
                    "price" to it.price,
                    "quantity" to it.quantity
                )
            }
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                Toast.makeText(this, "Order Sent Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send order. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

}