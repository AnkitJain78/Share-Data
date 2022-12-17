package com.example.firebase.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.firebase.MainActivity
import com.example.firebase.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    lateinit var editTextEmail: EditText
    lateinit var editTextPassword: EditText
    lateinit var buttonLogin: Button
    lateinit var buttonForgetPassword: Button
    lateinit var buttonLoginWithPhone: Button
    private lateinit var buttonSignUp: Button
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmailLogin)
        editTextPassword = findViewById(R.id.editTextPasswordLogin)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonSignUp = findViewById(R.id.buttonSignUpLogin)
        buttonForgetPassword = findViewById(R.id.buttonForgetPassword)
        buttonLoginWithPhone = findViewById(R.id.buttonLoginWithPhone)

        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            signIn(email, password)
        }

        buttonForgetPassword.setOnClickListener {
            val fragmentManager: FragmentManager = supportFragmentManager
            val dialogFragment = ForgetPassword()
            dialogFragment.show(fragmentManager, "fragment_add_user")
        }

        buttonLoginWithPhone.setOnClickListener {
            val intent = Intent(this, LoginPhoneActivity::class.java)
            startActivity(intent)
        }
    }


    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}