package com.example.firebase.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase.MainActivity
import com.example.firebase.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {
    lateinit var editTextPhoneNo: EditText
    lateinit var editTextCode: EditText
    lateinit var buttonSendCode: Button
    lateinit var buttonVerify: Button
    lateinit var callback1: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var smsCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone)

        editTextPhoneNo = findViewById(R.id.editTextPhone)
        editTextCode = findViewById(R.id.editTextCode)
        buttonSendCode = findViewById(R.id.buttonSendCode)
        buttonVerify = findViewById(R.id.buttonVerify)

        buttonSendCode.setOnClickListener {
            val inputNo = editTextPhoneNo.text.toString()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setActivity(this@LoginPhoneActivity)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setPhoneNumber(inputNo)
                .setCallbacks(callback1)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        callback1 = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                TODO("Not yet implemented")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                TODO("Not yet implemented")
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                smsCode = p0
                Log.d("code", smsCode)
            }
        }

        buttonVerify.setOnClickListener {
            verify()
        }
    }

    fun verify() {
        val userEnteredCode = editTextCode.text.toString()
        val credential = PhoneAuthProvider.getCredential(smsCode, userEnteredCode)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(this, "code is incorrect", Toast.LENGTH_SHORT).show()
        }

    }
}