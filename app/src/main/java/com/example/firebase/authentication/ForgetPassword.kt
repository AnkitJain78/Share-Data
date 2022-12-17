package com.example.firebase.authentication

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.firebase.R
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : DialogFragment() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var editTextEmail: EditText
    lateinit var buttonReset: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        val view = inflater.inflate(R.layout.fragment_forget_password, container, false)
        editTextEmail = view.findViewById(R.id.editTextEmailForget)
        buttonReset = view.findViewById(R.id.buttonReset)

        buttonReset.setOnClickListener {
            val email = editTextEmail.text.toString()
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "email to reset password id send to your email id",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog?.dismiss()
                }
            }
        }


        return view
    }

}