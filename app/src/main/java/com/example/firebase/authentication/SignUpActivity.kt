package com.example.firebase.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase.R
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    lateinit var editTextEmail: EditText
    lateinit var editTextPassword: EditText
    lateinit var buttonSignUp: Button
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        editTextEmail = findViewById(R.id.editTextEmailSignUp)
        editTextPassword = findViewById(R.id.editTextPasswordSignUp)
        buttonSignUp = findViewById(R.id.buttonSignUp)

        buttonSignUp.setOnClickListener {
            val userEmail: String = editTextEmail.text.toString()
            val userPassword: String = editTextPassword.text.toString()
            signUpWithFirebase(userEmail, userPassword)
        }
    }

    private fun signUpWithFirebase(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Your account has been created", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}