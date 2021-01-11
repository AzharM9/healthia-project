package com.example.healthia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.UserAdapter.MyHolder
import com.squareup.picasso.Picasso

class UserAdapter     //constructor
(var context: Context?, var userList: List<ModelUser?>?) : RecyclerView.Adapter<MyHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyHolder {
        //inflate layout(item_row_user.xml)
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_users, viewGroup, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data
        val userImage = userList!![position]?.image
        val userName = userList!![position]?.name
        val userEmail = userList!![position]?.email

        //set daata
        holder.mNameTv.text = userName
        holder.mEmailTv.text = userEmail
        try {
            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(holder.mAvatarTv)
        } catch (e: Exception) {
        }

        //handle item click
        holder.itemView.setOnClickListener { Toast.makeText(context, "" + userEmail, Toast.LENGTH_SHORT).show() }
    }

    override fun getItemCount(): Int {
        return userList!!.size
    }

    //view holder class
    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mAvatarTv: ImageView
        var mNameTv: TextView
        var mEmailTv: TextView

        init {

            //init views
            mAvatarTv = itemView.findViewById(R.id.avatarIv)
            mNameTv = itemView.findViewById(R.id.nameTv)
            mEmailTv = itemView.findViewById(R.id.emailTv)
        }
    }
}