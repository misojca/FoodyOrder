package com.example.foodyorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailRegisterInput)
        val passwordInput = findViewById<EditText>(R.id.passwordRegisterInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    saveUserToFirestore(userId, email)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("RegisterActivity", "Registration error", exception)
            }
    }

    private fun saveUserToFirestore(userId: String, email: String) {

        val user = hashMapOf(
            "email" to email,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RegisterActivity", "Firestore error", e)
            }
    }
}
