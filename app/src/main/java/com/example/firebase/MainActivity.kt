package com.example.firebase

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase.Adapter.UserAdapter
import com.example.firebase.Model.Users
import com.example.firebase.authentication.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var reference: DatabaseReference = firebaseDatabase.reference.child("Users")
    var usersList = ArrayList<Users>()
    var imageNameList = ArrayList<String>()
    lateinit var adapter: UserAdapter
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference
    lateinit var appSettingPrefs: SharedPreferences
    lateinit var sharedPrefsEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appSettingPrefs = getSharedPreferences("AppSettingPrefs",0)
        sharedPrefsEditor = appSettingPrefs.edit()
        val isNightModeOn : Boolean = appSettingPrefs.getBoolean("NightMode",false)
        if(isNightModeOn)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.floatingActionButton)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fab.setOnClickListener {
            val fragmentManager: FragmentManager = supportFragmentManager
            val dialogFragment = AddUser(this)
            dialogFragment.show(fragmentManager, "fragment_add_user")
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val userId = adapter.getUserId(viewHolder.adapterPosition)
                reference.child(userId).removeValue()

                val imageName = adapter.getImageName(viewHolder.adapterPosition)
                val imageReference = storageReference.child("images").child(imageName)
                imageReference.delete()
                Toast.makeText(this@MainActivity, "item deleted", Toast.LENGTH_SHORT).show()
            }

        }).attachToRecyclerView(recyclerView)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                usersList.clear()
                for (eachUser in p0.children) {
                    val user: Users? = eachUser.getValue(Users::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }

                adapter = UserAdapter(usersList, this@MainActivity)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "error in retrieving data", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.delete_all_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteAll -> showDialog()
            R.id.logOut -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.themeMenu -> themeChangerDialog()
        }
        return true
    }

    private fun themeChangerDialog() {
        val dialog = AlertDialog.Builder(this@MainActivity,R.style.AlertDialogCustom)
        dialog.setTitle("Which theme do you want?")
        dialog.setPositiveButton("Light", DialogInterface.OnClickListener { _, _ ->
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPrefsEditor.putBoolean("NightMode",false)
            sharedPrefsEditor.apply()
        })
        dialog.setNegativeButton("Dark", DialogInterface.OnClickListener { _, _ ->
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPrefsEditor.putBoolean("NightMode",true)
            sharedPrefsEditor.apply()
        })
        dialog.create().show()
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(this,R.style.AlertDialogCustom)
        dialog.setTitle("Delete all users")
        dialog.setMessage("Do you really want to delete all users. Swipe left to delete a particular user.")
        dialog.setPositiveButton("Yes") { _, _ ->

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (eachUser in snapshot.children) {
                        val user: Users? = eachUser.getValue(Users::class.java)
                        if (user != null) {
                            imageNameList.add(user.imageName)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            reference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("list", imageNameList.toString())
                    for (imageName in imageNameList) {
                        val imageReference = storageReference.child("images").child(imageName)
                        imageReference.delete()
                    }
                    Toast.makeText(this, "All users deleted successfully", Toast.LENGTH_SHORT)
                        .show()
                } else
                    Toast.makeText(this, "error in deletion", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.cancel()
        }
        dialog.create().show()
    }
}