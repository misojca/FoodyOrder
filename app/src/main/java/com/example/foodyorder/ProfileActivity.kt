package com.example.foodyorder

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailTextView: TextView = findViewById(R.id.tvEmail)
        val recyclerView: RecyclerView = findViewById(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val user = auth.currentUser

        if (user != null) {
            emailTextView.text = "Email: ${user.email}"

            db.collection("orders")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener { result ->

                    val orderList = mutableListOf<Order>()

                    for (document in result) {
                        val order = document.toObject(Order::class.java)
                        orderList.add(order)
                    }

                    recyclerView.adapter = OrderAdapter(orderList)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
