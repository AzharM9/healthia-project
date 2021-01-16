package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //views
    Button mRegisterBtn, mLoginBtn;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mRegisterBtn = findViewById(R.id.register_btn);
        mLoginBtn = findViewById(R.id.login_btn);

        //handle register button click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start RegisterActivity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            }
        });

        //handle login button click
       mLoginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               //start Login Activity
               startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
       });
    }
}

/*In this part 09
* ->Publish post to firebase.
*   post will contain user name, email, uid, uid, dp, time of publish, title description, image etc
*   User can publish post with and without image
*   Create AddPostActivity
*   Add another option in actionbar to go to AddPostActivity
*   Image can be imported from gallery or taken from camera*/
