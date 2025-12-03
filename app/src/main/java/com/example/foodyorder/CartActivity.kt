
package com.example.foodyorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.google.firebase.firestore.ListenerRegistration
import org.w3c.dom.Text

class CartActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var cartList = mutableListOf<Cart>()
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private var backPressedTime = 0L
    private var cartListener: ListenerRegistration? = null
    private lateinit var btnOrder: Button
    private lateinit var tvEmptyCartMessage: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        tvEmptyCartMessage = findViewById(R.id.tv_empty_cart_message)

        cartAdapter = CartAdapter(cartList)
        cartRecyclerView.adapter = cartAdapter

        btnOrder = findViewById(R.id.btn_order)
        btnOrder.isEnabled = false
        setupCartListener()

        btnOrder.setOnClickListener {
            placeOrder(cartList)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })

        onBackPressedDispatcher.addCallback(this) {

            if (backPressedTime + 2000 > System.currentTimeMillis()) return@addCallback

            backPressedTime = System.currentTimeMillis()

            val intent = Intent(this@CartActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

    }


    override fun onStop() {
        super.onStop()
        cartListener?.remove()
    }

    private fun setupCartListener(){
        val userId = auth.currentUser?.uid

        if(userId == null){
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }


        cartListener = db.collection("users")
            .document(userId)
            .collection("cart")
            .addSnapshotListener { snapshots, e ->

                if (e != null) {
                    Toast.makeText(this, "Error loading cart: ${e.message}", Toast.LENGTH_LONG).show()

                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newCartList = mutableListOf<Cart>()
                    for (document in snapshots.documents){


                        val item = document.toObject(Cart::class.java)


                        if (item != null) {
                            val itemWithId = item.copy(documentId = document.id)
                            newCartList.add(itemWithId)
                        }
                    }

                    cartAdapter.updateItems(newCartList)

                    val isCartEmpty = cartList.isEmpty()

                    btnOrder.isEnabled = newCartList.isNotEmpty()

                    if (isCartEmpty) {
                        tvEmptyCartMessage.visibility = View.VISIBLE
                        cartRecyclerView.visibility = View.GONE
                    } else {
                        tvEmptyCartMessage.visibility = View.GONE
                        cartRecyclerView.visibility = View.VISIBLE
                    }

                }
            }
    }



    private fun placeOrder(cartList: List<Cart>){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return


        val totalPrice = cartList.sumOf { it.price }

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

               // clearCartItems(userId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send order. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearCartItems(userId: String) {
        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Failed to clear cart after order: ${e.message}")
            }
    }

}