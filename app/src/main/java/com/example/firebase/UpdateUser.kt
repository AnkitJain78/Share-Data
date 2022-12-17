package com.example.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.firebase.Model.Users
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class UpdateUser(private val context1: Context) : DialogFragment() {

    private lateinit var editTextNameUpdate: EditText
    private lateinit var editTextAgeUpdate: EditText
    private lateinit var editTextEmailUpdate: EditText
    private lateinit var buttonUpdate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var profileImage: ImageView
    private lateinit var uri: Uri
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var isUriChange = false
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("Users")
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference
    private lateinit var userId: String
    private lateinit var imageUrl: String
    private lateinit var imageName: String
    private var name: String? = null
    private var age: String? = null
    private var email: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        val view = inflater.inflate(R.layout.fragment_update_user, container, false)
        editTextNameUpdate = view.findViewById(R.id.editTextNameUpdate)
        editTextAgeUpdate = view.findViewById(R.id.editTextAgeUpdate)
        editTextEmailUpdate = view.findViewById(R.id.editTextEmailUpdate)
        buttonUpdate = view.findViewById(R.id.buttonUpdate)
        progressBar = view.findViewById(R.id.progressBarUpdateUser)
        profileImage = view.findViewById(R.id.profilePhotoUpdateUser)

        getAndSet()
        register()

        Picasso.get().load(imageUrl).into(profileImage, object : Callback {
            override fun onSuccess() {
                progressBar.visibility = View.INVISIBLE
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, "Error in fetching profile image", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        buttonUpdate.setOnClickListener {
            updatePhoto()
        }

        profileImage.setOnClickListener {
            imageChooser()
        }

        return view
    }

    private fun imageChooser() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        activityResultLauncher.launch(intent)
    }

    private fun register() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK && data != null) {
                uri = data.data!!
                isUriChange = true
                Picasso.get().load(uri).into(profileImage).toString()
            }
        }
    }

    private fun getAndSet() {
        val name = arguments?.getString("Name")
        val age = arguments?.getInt("Age")
        val email = arguments?.getString("Email")
        userId = arguments?.getString("UserId").toString()
        imageUrl = arguments?.getString("ImageUrl").toString()
        imageName = arguments?.getString("ImageName").toString()

        editTextNameUpdate.setText(name)
        editTextAgeUpdate.setText(age.toString())
        editTextEmailUpdate.setText(email)

    }

    private fun updatePhoto() {
        if(checkNullValues())
            Toast.makeText(context, "Fill all details", Toast.LENGTH_SHORT).show()
        else {
            if (isUriChange) {
                progressBar.visibility = View.VISIBLE
                buttonUpdate.isClickable = false
                val imageReference = storageReference.child("images").child(imageName)
                imageReference.putFile(uri).addOnSuccessListener {
                    Toast.makeText(context1, "Image updated", Toast.LENGTH_SHORT).show()
                    val downloadReference = storageReference.child("images").child(imageName)
                    downloadReference.downloadUrl.addOnSuccessListener { uri ->
                        val changedImageUrl = uri.toString()
                        updateUserToDatabase(changedImageUrl)
                    }
                }.addOnFailureListener {
                    Toast.makeText(context1, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            } else updateUserToDatabase(imageUrl)
        }
    }

    private fun checkNullValues(): Boolean {
        name = editTextNameUpdate.text.toString()
        age = editTextAgeUpdate.text.toString()
        email = editTextEmailUpdate.text.toString()
        return name.isNullOrEmpty() || email.isNullOrEmpty() || age.isNullOrEmpty()
    }

    private fun updateUserToDatabase(imageUrl: String) {
            val user = Users(userId, name!!, age!!.toInt(), email!!, imageUrl, imageName)
            reference.child(userId).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "User Updated successfully", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                } else
                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
    }
}