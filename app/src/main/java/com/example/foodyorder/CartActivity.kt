package com.example.foodyorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CartActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var cartList: MutableList<Cart> = mutableListOf()

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var tvEmptyCartMessage: TextView
    private lateinit var btnOrder: Button
    private lateinit var tvTotalPrice: TextView

    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartRepository: CartRepository

    private var cartListener: ListenerRegistration? = null
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cartRepository = CartRepository()


        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        tvEmptyCartMessage = findViewById(R.id.tv_empty_cart_message)
        btnOrder = findViewById(R.id.btn_order)
        tvTotalPrice = findViewById(R.id.tvPrice)

        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(cartList, cartRepository)
        cartRecyclerView.adapter = cartAdapter


        setupCartListener()

        btnOrder.setOnClickListener {
            placeOrder()
        }

        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
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
            handleCartData(emptyList())
            return
        }

        cartListener = db.collection("users")
            .document(userId)
            .collection("cart")
            .addSnapshotListener { snapshots, e ->

                if (e != null) {
                    Toast.makeText(this, "Cart loading error ${e.message}", Toast.LENGTH_LONG).show()
                    handleCartData(emptyList())
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newCartList = mutableListOf<Cart>()
                    var totalAmount = 0.0

                    for (document in snapshots.documents){
                        try {
                            val item = document.toObject(Cart::class.java)
                            val documentId = document.id
                            val finalCartItem = item?.copy(documentId = documentId)

                            if (finalCartItem != null) {
                                newCartList.add(finalCartItem)

                                totalAmount += finalCartItem.price
                                Log.d("CartActivity", "Mapping successful for doceumnt: $documentId")
                            } else {
                                Log.e("CartActivity", "Mapping failed for document: ${document.id}")
                            }
                        } catch (ex: Exception) {
                            Log.e("CartActivity", "Mapping exception: ${document.id}", ex)
                        }
                    }

                    handleCartData(newCartList, totalAmount)
                }
            }
    }

    private fun handleCartData(data: List<Cart>, totalAmount: Double = 0.0) {

        cartAdapter.updateItems(data)

        val isCartEmpty = data.isEmpty()
        btnOrder.isEnabled = !isCartEmpty

        if (isCartEmpty) {
            tvEmptyCartMessage.visibility = View.VISIBLE
            cartRecyclerView.visibility = View.GONE
        } else {
            tvEmptyCartMessage.visibility = View.GONE
            cartRecyclerView.visibility = View.VISIBLE
          //  tvTotalPrice.text = String.format("Ukupno: %.2f e", totalAmount)

        }
    }


    private fun placeOrder(){
        val callback = object : CartOperationCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(this@CartActivity, message, Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onFailure(exception: Exception, message: String) {
                Toast.makeText(this@CartActivity, "$message: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
        cartRepository.placeOrder(cartList, callback)
    }
}