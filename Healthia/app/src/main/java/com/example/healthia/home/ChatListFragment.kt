package com.example.healthia.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.MainActivity
import com.example.healthia.R
import com.example.healthia.adapters.AdapterChatlist
import com.example.healthia.models.ModelChat
import com.example.healthia.models.ModelChatlist
import com.example.healthia.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class ChatListFragment : Fragment() {
    //firebase Auth
    var firebaseAuth: FirebaseAuth? = null
    var recyclerView: RecyclerView? = null
    var chatlistList: MutableList<ModelChatlist?>? = null
    var userList: MutableList<ModelUser?>? = null
    var refence: DatabaseReference? = null
    var currentUser: FirebaseUser? = null
    var adapterChatlist: AdapterChatlist? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)

        // init
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser
        recyclerView = view.findViewById(R.id.recyclerView)
        chatlistList = ArrayList()
        refence = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser!!.uid)
        refence!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatlistList as ArrayList<ModelChatlist?>).clear()
                for (ds in snapshot.children) {
                    val chatlist = ds.getValue(ModelChatlist::class.java)
                    (chatlistList as ArrayList<ModelChatlist?>).add(chatlist)
                }
                loadChats()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return view
    }

    private fun loadChats() {
        userList = ArrayList()
        refence = FirebaseDatabase.getInstance().getReference("Users")
        refence!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<ModelUser?>).clear()
                for (ds in snapshot.children) {
                    val user = ds.getValue(ModelUser::class.java)
                    for (chatlist in chatlistList!!) {
                        if (user?.uid != null && user.uid == chatlist?.id) {
                            (userList as ArrayList<ModelUser?>).add(user)
                            break
                        }
                    }
                    // adapter
                    adapterChatlist = AdapterChatlist(context, userList as ArrayList<ModelUser?>)
                    // set adapter
                    recyclerView!!.adapter = adapterChatlist
                    // set last message
                    for (i in (userList as ArrayList<ModelUser?>).indices) {
                        lastMessage((userList as ArrayList<ModelUser?>)[i]?.uid)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun lastMessage(uid: String?) {
        val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var theLastMessage = "default"
                for (ds in snapshot.children) {
                    val chat = ds.getValue(ModelChat::class.java) ?: continue
                    val sender = chat.sender
                    val receiver = chat.receiver
                    if (sender == null || receiver == null) {
                        continue
                    }
                    if (chat.receiver == currentUser!!.uid && chat.sender == uid ||
                            chat.receiver == uid && chat.sender == currentUser!!.uid) {
                        theLastMessage = chat.message.toString()
                    }
                }
                adapterChatlist!!.setLastMessageMap(uid, theLastMessage)
                adapterChatlist!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
//            mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }
    }
}