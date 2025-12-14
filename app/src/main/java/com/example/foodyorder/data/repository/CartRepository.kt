package com.example.foodyorder.data.repository

import android.util.Log
import com.example.foodyorder.utils.CartOperationCallback
import com.example.foodyorder.data.model.Cart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "CartRepository"
    private fun getUserId(): String? {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "getUserId: Current user ID: $userId")
        return userId
    }

    private fun getCartItemReference(documentId: String) = getUserId()?.let { userId ->
        val ref = db.collection("users")
            .document(userId)
            .collection("cart")
            .document(documentId)
        Log.d(TAG, "getCartItemReference: Reference path: ${ref.path}")
        ref
    }

    fun updateCartItemQuantity(cartItem: Cart, newQuantity: Int, unitPrice: Double, callback: CartOperationCallback) {

        if (newQuantity <= 0) {
            Log.w(TAG, "New quantity is 0 or less. Triggering deleteCartItem for ${cartItem.name}.")
            deleteCartItem(cartItem, callback)
            return
        }

        val itemRef = getCartItemReference(cartItem.documentId)
        if (itemRef == null) {
            Log.e(TAG, "updateCartItemQuantity: Null item reference (User not logged in or missing Doc ID).")
            callback.onFailure(IllegalStateException("User not logged in or item ID missing."), "User not logged in or item ID missing.")
            return
        }

        val newTotalPrice = unitPrice * newQuantity.toDouble()
        Log.d(TAG, "Calculated new total price for ${cartItem.name}: $newTotalPrice")

        itemRef.update(
            "quantity", newQuantity,
            "price", newTotalPrice
        ).addOnSuccessListener {
            Log.d(TAG, "SUCCESS: Quantity updated for ${cartItem.name} to $newQuantity.")
            callback.onSuccess("The amount of dish ${cartItem.name} has been updated to $newQuantity.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "ERROR updating quantity for ${cartItem.name}", e)
            callback.onFailure(e, "Updating error for ${cartItem.name}.")
        }
    }

    fun deleteCartItem(cartItem: Cart, callback: CartOperationCallback) {
        Log.i(TAG, "deleteCartItem called for ${cartItem.name}. Document ID: ${cartItem.documentId}")
        val itemRef = getCartItemReference(cartItem.documentId)
        if (itemRef == null) {
            Log.e(TAG, "deleteCartItem: Null item reference (User not logged in or missing Doc ID).")
            callback.onFailure(IllegalStateException("User not logged in or item ID missing."), "User not logged in or item ID missing.")
            return
        }

        itemRef.delete().addOnSuccessListener {
            Log.d(TAG, "SUCCESS: Dish ${cartItem.name} deleted successfully.")
            callback.onSuccess("Dish ${cartItem.name} removed from the cart.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "ERROR deleting item: ${cartItem.name}", e)
            callback.onFailure(e, "Deleting error for ${cartItem.name}.")
        }
    }

    fun placeOrder(cartList: List<Cart>, callback: CartOperationCallback) {
        Log.i(TAG, "placeOrder called with ${cartList.size} items.")
        val userId = getUserId()
        if (userId == null) {
            Log.e(TAG, "placeOrder: User not logged in, order placement failed.")
            callback.onFailure(IllegalStateException("User not logged in."), "User not logged in, cannot place order.")
            return
        }

        val totalPrice = cartList.sumOf { it.price }
        Log.d(TAG, "Order total price: $totalPrice")

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
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "SUCCESS: Order placed. Document ID: ${docRef.id}")
                callback.onSuccess("Order Sent Successfully!")

                clearCartItems(userId, object : CartOperationCallback {
                    override fun onSuccess(message: String) {
                        Log.d(TAG, "SUCCESS: Cart cleared after order.")
                    }
                    override fun onFailure(exception: Exception, message: String) {
                        Log.e(TAG, "WARNING: Failed to clear cart after order: $message", exception)
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ERROR: Failed to send order.", e)
                callback.onFailure(e, "Failed to send order. Try again.")
            }
    }

    private fun clearCartItems(userId: String, callback: CartOperationCallback) {
        Log.d(TAG, "clearCartItems started for user: $userId")
        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Found ${result.documents.size} items to clear in cart.")
                val batch = db.batch()
                result.documents.forEach { document ->
                    batch.delete(document.reference)
                    Log.v(TAG, "Adding delete operation for document: ${document.id}")
                }
                batch.commit().addOnSuccessListener {
                    Log.d(TAG, "SUCCESS: Batch delete committed.")
                    callback.onSuccess("Cart items cleared.")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "ERROR: Failed to execute batch delete.", e)
                    callback.onFailure(e, "Failed to execute batch delete.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ERROR: Failed to retrieve cart items for clearing.", e)
                callback.onFailure(e, "Failed to retrieve cart items for clearing.")
            }
    }

}