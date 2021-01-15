package com.example.healthia.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.healthia.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //get current user from shared preferences
        val sp = getSharedPreferences("SP_USER", MODE_PRIVATE)
        val savedCurrentUser = sp.getString("Current_USERID", "None")
        val sent = remoteMessage.data["sent"]
        val user = remoteMessage.data["user"]
        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser != null && sent == fUser.uid) {
            if (savedCurrentUser != user) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOandAboveNotification(remoteMessage)
                } else {
                    sendNormalNotification(remoteMessage)
                }
            }
        }
    }

    private fun sendNormalNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val notification = remoteMessage.notification
        val i = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("hisUid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT)
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(icon!!.toInt())
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var j = 0
        if (i > 0) {
            j = i
        }
        notificationManager.notify(j, builder.build())
    }

    private fun sendOandAboveNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val notification = remoteMessage.notification
        val i = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("hisUid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT)
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification1 = OreoAndAboveNotification(this)
        val builder = notification1.getOnNotifications(title, body, pIntent, defSoundUri, icon)
        var j = 0
        if (i > 0) {
            j = i
        }
        notification1.manager?.notify(j, builder!!.build())
    }
}