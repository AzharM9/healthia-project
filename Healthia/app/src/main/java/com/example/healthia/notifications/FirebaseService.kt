package com.example.healthia.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class FirebaseService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val user = FirebaseAuth.getInstance().currentUser
        val tokenRefresh = FirebaseInstanceId.getInstance().token
        if (user != null) {
            updateToken(tokenRefresh)
        }
    }

    private fun updateToken(tokenRefresh: String?) {
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(tokenRefresh)
        ref.child(user!!.uid).setValue(token)
    }
}