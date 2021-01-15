package com.example.healthia.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.R
import com.example.healthia.models.ModelChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*

class AdapterChat(var context: Context, var chatList: MutableList<ModelChat?>?, var imageUrl: String) : RecyclerView.Adapter<AdapterChat.MyHolder>() {
    var fUser: FirebaseUser? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
        //inflate layouts; row_chat_left.xml for receiver, row_chat_right.xml for sender
        return if (i == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false)
            MyHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false)
            MyHolder(view)
        }
    }

    override fun onBindViewHolder(myHolder: MyHolder, i: Int) {
        // get data
        val message = chatList?.get(i)?.message
        val timestamp = chatList?.get(i)?.timestamp

        //convert time stamp
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp?.toLong() ?: 0
        val datetime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()

        //set data
        myHolder.messageTv.text = message
        myHolder.timeTv.text = datetime
        try {
            Picasso.get().load(imageUrl).into(myHolder.profileIv)
        } catch (e: Exception) {
        }

        //click to show delete dialog
        myHolder.messageLayout.setOnClickListener { //show delete message confirm dialog
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
            builder.setMessage("Are you sure to delete this message?")
            //delete button
            builder.setPositiveButton("Delete") { dialog, which -> deleteMessage(i) }
            //cancel delete button
            builder.setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            // create and show dialog
            builder.create().show()
        }

        //set seen/delivered
        if (i == chatList!!.size - 1) {
            if (chatList!![i]!!.isSeen) {
                myHolder.isSeenTv.text = "Seen"
            } else {
                myHolder.isSeenTv.text = "Delivered"
            }
        } else {
            myHolder.isSeenTv.visibility = View.GONE
        }
    }

    private fun deleteMessage(position: Int) {
        val myUID = FirebaseAuth.getInstance().currentUser!!.uid
        val msgTimeStamp = chatList?.get(position)?.timestamp
        val dbRef = FirebaseDatabase.getInstance().getReference("Chats")
        val query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    if (ds.child("sender").value == myUID) {
                        //1) Remove the message from chat
                        //ds.getRef().removeValue();

                        //2) set the value of message "This message was deleted..."
                        val hashMap = HashMap<String, Any>()
                        hashMap["message"] = "This message was deleted..."
                        ds.ref.updateChildren(hashMap)
                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return chatList?.size!!
    }

    override fun getItemViewType(position: Int): Int {
        //get currently sign in user
        fUser = FirebaseAuth.getInstance().currentUser
        return if (chatList?.get(position)?.sender == fUser!!.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    // view holder class
    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //views
        var profileIv: ImageView
        var messageTv: TextView
        var timeTv: TextView
        var isSeenTv: TextView
        var messageLayout: LinearLayout

        init {

            //init views
            profileIv = itemView.findViewById(R.id.profileIv)
            messageTv = itemView.findViewById(R.id.messageTv)
            timeTv = itemView.findViewById(R.id.timeTv)
            isSeenTv = itemView.findViewById(R.id.isSeenTv)
            messageLayout = itemView.findViewById(R.id.messageLayout)
        }
    }

    companion object {
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_RIGHT = 1
    }
}