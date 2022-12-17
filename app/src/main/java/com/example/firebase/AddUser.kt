package com.example.firebase

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.firebase.Model.Users
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

class AddUser(val context1: Context) : DialogFragment() {
    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonOk: Button
    private lateinit var profileImage: ImageView
    private lateinit var progressBar: ProgressBar
    lateinit var uri: Uri
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("Users")
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference
    private var name: String? = null
    private var age: String? = null
    private var email: String? = null
    private var isImagePicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        val view = inflater.inflate(R.layout.fragment_add_user, container, false)
        editTextName = view.findViewById(R.id.editTextName)
        editTextAge = view.findViewById(R.id.editTextAge)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        buttonOk = view.findViewById(R.id.buttonAdd)
        profileImage = view.findViewById(R.id.profilePhotoAddUser)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
        register()
        buttonOk.setOnClickListener {
            uploadPhoto()
        }

        profileImage.setOnClickListener {
            imageChooser()
        }
        return view
    }

    private fun imageChooser() {
        if (ContextCompat.checkSelfPermission(
                context1,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                MainActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        else {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }

    private fun addUserToDatabase(imageUrl: String, imageName: String) {
        val userId: String = reference.push().key.toString()
        val user = Users(userId, name!!, age!!.toInt(), email!!, imageUrl, imageName)
        reference.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "User added successfully", Toast.LENGTH_LONG).show()
                buttonOk.isClickable = true
                progressBar.visibility = View.INVISIBLE
                dialog?.dismiss()
            } else {
                Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
                buttonOk.isClickable = true
                progressBar.visibility = View.INVISIBLE
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }

    private fun register() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == RESULT_OK && data != null) {
                uri = data.data!!
                Picasso.get().load(uri).into(profileImage).toString()
                isImagePicked = true
            }
        }
    }

    private fun uploadPhoto() {
        if(checkNullValues())
            Toast.makeText(context1,"Please fill all details",Toast.LENGTH_SHORT).show()
        else {
            progressBar.visibility = View.VISIBLE
            buttonOk.isClickable = false
            val imageName = UUID.randomUUID().toString()
            val imageReference = storageReference.child("images").child(imageName)
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(context1, "Image uploaded", Toast.LENGTH_SHORT).show()
                val downloadReference = storageReference.child("images").child(imageName)
                downloadReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    addUserToDatabase(imageUrl, imageName)
                }
            }.addOnFailureListener {
                Toast.makeText(context1, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkNullValues(): Boolean {
        name = editTextName.text.toString()
        age = editTextAge.text.toString()
        email = editTextEmail.text.toString()
        return name.isNullOrEmpty() || email.isNullOrEmpty() || age.isNullOrEmpty() || !isImagePicked
    }
}