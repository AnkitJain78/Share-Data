package com.example.firebase.Adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase.Model.Users
import com.example.firebase.R
import com.example.firebase.UpdateUser
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class UserAdapter(private val userList: ArrayList<Users>, private val context: Context) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textViewName)
        val age: TextView = itemView.findViewById(R.id.textViewAge)
        val email: TextView = itemView.findViewById(R.id.textViewEmail)
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val cardView: CardView = itemView.findViewById(R.id.cardViewItemUser)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.name.text = currentUser.name
        holder.age.text = currentUser.age.toString()
        holder.email.text = currentUser.email
        val imageUrl: String = currentUser.imageUrl
        Log.d("url", imageUrl)
        Picasso.get().load(imageUrl).into(holder.profileImage, object : Callback {
            override fun onSuccess() {
                holder.progressBar.visibility = View.INVISIBLE
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, "error in fetching image", Toast.LENGTH_SHORT).show()
            }
        })

        holder.cardView.setOnClickListener {
            val mBundle = Bundle()
            mBundle.putString("Name", currentUser.name)
            mBundle.putInt("Age", currentUser.age)
            mBundle.putString("Email", currentUser.email)
            mBundle.putString("UserId", currentUser.userId)
            mBundle.putString("ImageUrl", currentUser.imageUrl)
            mBundle.putString("ImageName", currentUser.imageName)

            val fragmentManager: FragmentManager =
                (context as AppCompatActivity).supportFragmentManager
            val dialogFragment = UpdateUser(context)
            dialogFragment.arguments = mBundle
            dialogFragment.show(fragmentManager, "fragment_update_user")
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun getUserId(position: Int): String {
        return userList[position].userId
    }

    fun getImageName(position: Int): String {
        return userList[position].imageName
    }
}