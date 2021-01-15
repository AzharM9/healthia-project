package com.example.healthia

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.adapters.AdapterChat
import com.example.healthia.models.ModelChat
import com.example.healthia.models.ModelUser
import com.example.healthia.notifications.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class ChatActivity : AppCompatActivity() {
    //views from xml
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var profileIv: ImageView? = null
    var nameTv: TextView? = null
    var userStatusTv: TextView? = null
    var messageEt: EditText? = null
    var sendBtn: ImageButton? = null

    //firebase auth
    var firebaseAuth: FirebaseAuth? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var usersDbRef: DatabaseReference? = null

    //for checking if use has seen message or not
    var seenListener: ValueEventListener? = null
    var userRefForSeen: DatabaseReference? = null
    var chatList: MutableList<ModelChat?>? = null
    var adapterChat: AdapterChat? = null
    var hisUid: String? = null
    var myUid: String? = null
    var hisImage: String? = null
    var apiService: APIService? = null
    var notify = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //init views
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = ""
        recyclerView = findViewById(R.id.chat_recyclerView)
        profileIv = findViewById(R.id.profileIv)
        nameTv = findViewById(R.id.nameTv)
        userStatusTv = findViewById(R.id.userStatusTv)
        messageEt = findViewById(R.id.messageEt)
        sendBtn = findViewById(R.id.sendBtn)

        //layout (linearlayout) for recyclerview
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        //recyclerview properties
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = linearLayoutManager

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/")!!.create(APIService::class.java)
        val intent = intent
        hisUid = intent.getStringExtra("hisUid")

        //firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersDbRef = firebaseDatabase!!.getReference("Users")

        //search user to get user's info
        val userQuery = usersDbRef!!.orderByChild("uid").equalTo(hisUid)

        //get user's picture and name
        userQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //check until required info is received
                for (ds in snapshot.children) {
                    //get data
                    val name = "" + ds.child("name").value
                    hisImage = "" + ds.child("image").value
                    //get value online status
                    val onlineStatus = "" + ds.child("onlineStatus").value
                    if (onlineStatus == "online") {
                        userStatusTv?.text = onlineStatus
                    } else {
                        // convert time stamp to proper time date
                        val cal = Calendar.getInstance(Locale.ENGLISH)
                        cal.timeInMillis = onlineStatus.toLong()
                        val datetime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()
                        userStatusTv?.text = "Last seen at: $datetime"
                    }

                    //set data
                    nameTv?.text = name
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // click button to send message
        sendBtn?.setOnClickListener(View.OnClickListener {
            notify = true
            //get text from edit text
            val message = messageEt?.text.toString().trim { it <= ' ' }
            //check if text is empty or not
            if (TextUtils.isEmpty(message)) {
                //text empty
                Toast.makeText(this@ChatActivity, "Cannot send empty message...", Toast.LENGTH_SHORT).show()
            } else {
                //text not empty
                sendMessage(message)
            }
            //reset edit text after sending message
            messageEt?.setText("")
        })
        readMessage()
        seenMessage()
    }

    private fun seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = userRefForSeen!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val chat = ds.getValue(ModelChat::class.java)
                    if (chat?.receiver == myUid && chat?.sender == hisUid) {
                        val hasSeenMap = HashMap<String, Any>()
                        hasSeenMap["isSeen"] = true
                        ds.ref.updateChildren(hasSeenMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun readMessage() {
        chatList = ArrayList()
        val dbRef = FirebaseDatabase.getInstance().getReference("Chats")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<ModelChat?>).clear()
                for (ds in snapshot.children) {
                    val chat = ds.getValue(ModelChat::class.java)
                    if (chat?.receiver == myUid && chat?.sender == hisUid ||
                            chat?.receiver == hisUid && chat?.sender == myUid) {
                        (chatList as ArrayList<ModelChat?>).add(chat)
                    }
                    // adapter
                    adapterChat = AdapterChat(this@ChatActivity, chatList as ArrayList<ModelChat?>, hisImage)
                    adapterChat!!.notifyDataSetChanged()
                    //set adapter to recyclerview
                    recyclerView!!.adapter = adapterChat
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(message: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val timestamp = System.currentTimeMillis().toString()
        val hashMap = HashMap<String, Any?>()
        hashMap["sender"] = myUid
        hashMap["receiver"] = hisUid
        hashMap["message"] = message
        hashMap["timestamp"] = timestamp
        hashMap["isSeen"] = false
        databaseReference.child("Chats").push().setValue(hashMap)
        val database = FirebaseDatabase.getInstance().getReference("Users").child(myUid!!)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(ModelUser::class.java)
                if (notify) {
                    sendNotification(hisUid, user?.name, message)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //create chatList node/child in firebase database
        val chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid!!)
                .child(hisUid!!)
        chatRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("id").setValue(hisUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        val chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid!!)
                .child(myUid!!)
        chatRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(myUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(hisUid: String?, name: String?, message: String) {
        val allTokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = allTokens.orderByKey().equalTo(hisUid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val token = ds.getValue(Token::class.java)
                    val data = Data(myUid, "$name: $message", "New Message", hisUid, R.drawable.ic_default_img)
                    val sender = Sender(data, token?.token)
                    apiService!!.sendNotification(sender)
                            .enqueue(object : Callback<Response?> {
                                override fun onResponse(call: Call<Response?>, response: retrofit2.Response<Response?>) {
                                    Toast.makeText(this@ChatActivity, "" + response.message(), Toast.LENGTH_SHORT).show()
                                }

                                override fun onFailure(call: Call<Response?>, t: Throwable) {}
                            })
                }
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
            //mProfileTv.setText(user.getEmail());
            myUid = user.uid //currently sign in user's
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(this, LoginOrRegisterActivity::class.java))
            finish()
        }
    }

    private fun checkOnlineStatus(status: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid!!)
        val hashMap = HashMap<String, Any>()
        hashMap["onlineStatus"] = status
        //update value of online status of current user
        dbRef.updateChildren(hashMap)
    }

    override fun onStart() {
        checkUserStatus()
        //set online
        checkOnlineStatus("online")
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        val timestamp = System.currentTimeMillis().toString()

        //set offline with last seen timestamp
        checkOnlineStatus(timestamp)
        userRefForSeen!!.removeEventListener(seenListener!!)
    }

    override fun onResume() {
        //set online
        checkOnlineStatus("online")
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        //hide search view
        menu.findItem(R.id.action_search).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id
        val id = item.itemId
        if (id == R.id.action_logout) {
            val timestamp = System.currentTimeMillis().toString()

            //set offline with last seen timestamp
            checkOnlineStatus(timestamp)
            firebaseAuth!!.signOut()
            checkUserStatus()
        }
        return super.onOptionsItemSelected(item)
    }
}