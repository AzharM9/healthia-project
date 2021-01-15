package com.example.healthia.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.ChatActivity
import com.example.healthia.models.ModelUser
import com.example.healthia.R
import com.squareup.picasso.Picasso

class UserAdapter     //constructor
    (var context: Context, var userList: MutableList<ModelUser?>?) : RecyclerView.Adapter<UserAdapter.MyHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyHolder {
        //inflate layout(item_row_user.xml)
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_users, viewGroup, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data
        val hisUID = userList?.get(position)?.uid
        val userImage = userList?.get(position)?.image
        val userName = userList?.get(position)?.name
        val userEmail = userList?.get(position)?.email

        //set daata
        holder.mNameTv.text = userName
        holder.mEmailTv.text = userEmail
        try {
            Picasso.get()
                .load(userImage)
                .placeholder(R.drawable.ic_default)
                .into(holder.mAvatarTv)
        } catch (e: Exception) {
        }

        //handle item click
        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("hisUid", hisUID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList?.size!!
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