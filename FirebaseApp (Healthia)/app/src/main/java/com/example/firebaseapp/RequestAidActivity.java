package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class RequestAidActivity extends AppCompatActivity {

    TextView nameTv, req_timeTv, addressTv;
    Button callBtn;
    CircleImageView userIv;
    String wId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_aid);

        getSupportActionBar().setTitle("Request Aid");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTv = findViewById(R.id.friendName);
        req_timeTv = findViewById(R.id.req_time);
        addressTv = findViewById(R.id.shared_location);
        userIv = findViewById(R.id.friendDp);
        callBtn = findViewById(R.id.callBtn);

        //get id of the post using intent
        Intent intent = getIntent();
        wId = intent.getStringExtra("wId");

        loadRequestData();

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "118";
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });
    }

    private void loadRequestData() {
        DatabaseReference mReqAidRef = FirebaseDatabase.getInstance().getReference("Request_aid");
        mReqAidRef.orderByChild("wId").equalTo(wId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("uName").getValue();
                    String dp = "" + ds.child("uDp").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    String address = "" + ds.child("wAddress").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String req_time = DateFormat.format("MMM d, yyyy hh:mm aa", calendar).toString();

                    nameTv.setText(name);
                    if (dp.equals("")) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_default_img)
                                .placeholder(R.drawable.ic_default_img)
                                .into(userIv);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(dp)
                                .placeholder(R.drawable.ic_default_img)
                                .into(userIv);
                    }

                    req_timeTv.setText("Request created at: "+req_time);
                    addressTv.setText("Your friend location: "+address);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}