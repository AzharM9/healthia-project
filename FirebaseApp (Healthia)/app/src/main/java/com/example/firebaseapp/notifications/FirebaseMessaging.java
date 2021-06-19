package com.example.firebaseapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.firebaseapp.ForumDetailActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.RequestAidActivity;
import com.example.firebaseapp.activitys.ChatActivity;
import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.PostDetailActivity;
import com.example.firebaseapp.activitys.ThereProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {

    Intent intent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get current user from shared preferences
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null && sent.equals(fUser.getUid())){
            if (!savedCurrentUser.equals(user)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOandAboveNotification(remoteMessage);
                }
                else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }

    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String postId = remoteMessage.getData().get("postId");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        if (title.equals("New Message")){
            intent = new Intent(this, ChatActivity.class);
        }
        else if (title.equals("New Post Comment") || title.equals("New Post Like")){
            intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", postId);
        }
        else if (title.equals("New Forum Reply")){
            intent = new Intent(this, ForumDetailActivity.class);
            intent.putExtra("postId", postId);
        }
        else if (title.equals("New Urgent Request")){
            intent = new Intent(this, RequestAidActivity.class);
            intent.putExtra("wId", postId);
        }
        else if (title.equals("New Friend Request")){
            intent = new Intent(this, ThereProfileActivity.class);
            intent.putExtra("uid", postId);
        }

        intent.putExtra("hisUid", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());
    }

    private void sendOandAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String postId = remoteMessage.getData().get("postId");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        if (title.equals("New Message")){
            intent = new Intent(this, ChatActivity.class);
        }
        else if (title.equals("New Urgent Request")){
            intent = new Intent(this, RequestAidActivity.class);
            intent.putExtra("wId", postId);
        }
        else if (title.equals("New Post Comment") || title.equals("New Post Like")){
            intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", postId);
        }
        else if (title.equals("New Forum Reply")){
            intent = new Intent(this, ForumDetailActivity.class);
            intent.putExtra("postId", postId);
        }
        else if (title.equals("New Friend Request")){
            intent = new Intent(this, ThereProfileActivity.class);
            intent.putExtra("uid", postId);
        }

        intent.putExtra("hisUid", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getOnNotifications(title, body, pIntent, defSoundUri, icon);

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());
    }

}
