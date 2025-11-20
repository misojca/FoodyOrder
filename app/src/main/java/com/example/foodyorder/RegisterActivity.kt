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

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailRegisterInput)
        val passwordInput = findViewById<EditText>(R.id.passwordRegisterInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Register successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterActivity", "Registration error", exception)
                }
        }
    }
}
