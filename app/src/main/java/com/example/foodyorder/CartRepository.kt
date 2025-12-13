package com.example.foodyorder

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "CartRepository"
/*
    interface CartOperationCallback {
        fun onSuccess(message: String)
        fun onFailure(exception: Exception, message: String)
    }
*/
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun getCartItemReference(documentId: String) = getUserId()?.let { userId ->
        db.collection("users")
            .document(userId)
            .collection("cart")
            .document(documentId)
    }

    fun updateCartItemQuantity(cartItem: Cart, newQuantity: Int, unitPrice: Double, callback: CartOperationCallback) {
        val itemRef = getCartItemReference(cartItem.documentId)
        if (itemRef == null) {
            callback.onFailure(IllegalStateException("User not logged in or item ID missing."), "User not logged in or item ID missing.")
            return
        }

        val newTotalPrice = unitPrice * newQuantity.toDouble()

        itemRef.update(
            "quantity", newQuantity,
            "price", newTotalPrice
        ).addOnSuccessListener {
            callback.onSuccess("The amount of dish ${cartItem.name} has been updated to $newQuantity.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error updating quantity for ${cartItem.name}", e)
            callback.onFailure(e, "Updating error for ${cartItem.name}.")
        }
    }

    fun deleteCartItem(cartItem: Cart, callback: CartOperationCallback) {
        val itemRef = getCartItemReference(cartItem.documentId)
        if (itemRef == null) {
            callback.onFailure(IllegalStateException("User not logged in or item ID missing."), "User not logged in or item ID missing.")
            return
        }

        itemRef.delete().addOnSuccessListener {
            callback.onSuccess("Dish ${cartItem.name} removed from the cart.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error deleting item: ${cartItem.name}", e)
            callback.onFailure(e, "Deleting error for ${cartItem.name}.")
        }
    }

    fun placeOrder(cartList: List<Cart>, callback: CartOperationCallback) {
        val userId = getUserId()
        if (userId == null) {
            callback.onFailure(IllegalStateException("User not logged in."), "User not logged in, cannot place order.")
            return
        }

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
                callback.onSuccess("Order Sent Successfully!")

                clearCartItems(userId, object : CartOperationCallback {
                    override fun onSuccess(message: String) {
                        Log.d(TAG, "Cart cleared successfully after order.")
                    }
                    override fun onFailure(exception: Exception, message: String) {
                        Log.e(TAG, "Warning: Failed to clear cart after order: $message", exception)
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send order.", e)
                callback.onFailure(e, "Failed to send order. Try again.")
            }
    }


    private fun clearCartItems(userId: String, callback: CartOperationCallback) {
        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                // Kreira listu obećanja za brisanje dokumenata
                val batch = db.batch()
                result.documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().addOnSuccessListener {
                    callback.onSuccess("Cart items cleared.")
                }.addOnFailureListener { e ->
                    callback.onFailure(e, "Failed to execute batch delete.")
                }
            }
            .addOnFailureListener { e ->
                callback.onFailure(e, "Failed to retrieve cart items for clearing.")
            }
    }
}