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
import com.example.healthia.R
import com.example.healthia.models.ModelUser
import com.squareup.picasso.Picasso
import java.util.*

class AdapterChatlist(var context: Context?, var userList: List<ModelUser?>) : RecyclerView.Adapter<AdapterChatlist.MyHolder>() {
    private val lastMessageMap: HashMap<String?, String?>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //inflate layout row_chat_list.xml
        val view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data
        val hisUid = userList[position]?.uid
        val userImage = userList[position]?.image
        val userName = userList[position]?.name
        val lastMessage = lastMessageMap[hisUid]

        //set data
        holder.nameTv.text = userName
        if (lastMessage == null || lastMessage == "default") {
            holder.lastMessageTv.visibility = View.GONE
        } else {
            holder.lastMessageTv.visibility = View.VISIBLE
            holder.lastMessageTv.text = lastMessage
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv)
        } catch (e: Exception) {
            Picasso.get().load(R.drawable.ic_default_img).into(holder.profileIv)
        }
        //set online status of other users in chatlist
        if (userList[position]?.onlineStatus == "online") {
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online)
        } else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline)
        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener { //start chat activity with that user
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("hisUid", hisUid)
            context!!.startActivity(intent)
        }
    }

    fun setLastMessageMap(userId: String?, lastMessage: String?) {
        lastMessageMap[userId] = lastMessage
    }

    override fun getItemCount(): Int {
        //size of the list
        return userList.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // views of row_chatlist.xml
        var profileIv: ImageView
        var onlineStatusIv: ImageView
        var nameTv: TextView
        var lastMessageTv: TextView

        init {

            //init views
            profileIv = itemView.findViewById(R.id.profileIv)
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv)
            nameTv = itemView.findViewById(R.id.nameTv)
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv)
        }
    }

    //constructor
    init {
        lastMessageMap = HashMap()
    }
}