package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class FullScreenImageActivity extends AppCompatActivity {

    String postId, postType, refPath, childKey, imageKey;
    ActionBar actionBar;
    PhotoView imagePv;
    Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        actionBar = getSupportActionBar();
        actionBar.hide();

        imagePv = findViewById(R.id.imgDisplay);
        btnClose = findViewById(R.id.btnClose);

        //get id of the post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postType = intent.getStringExtra("type");

        loadImage();

        btnClose.setOnClickListener(v->{
            onBackPressed();
        });
    }

    private void loadImage(){

        switch (postType){
            case ("Posts"):
                refPath = "Posts";
                childKey = "pId";
                imageKey = "pImage";
                break;
            case ("Articles"):
                refPath = "Articles";
                childKey = "aId";
                imageKey = "aImage";
                break;
            case ("Forums"):
                refPath = "Forums";
                childKey = "fId";
                imageKey = "fImage";
                break;

        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(refPath);
        Query query = ref.orderByChild(childKey).equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){
                    String imageName = ""+ds.child(imageKey).getValue();

                    Glide.with(FullScreenImageActivity.this)
                            .load(imageName)
                            .placeholder(R.drawable.ic_image_black_24)
                            .into(imagePv);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}