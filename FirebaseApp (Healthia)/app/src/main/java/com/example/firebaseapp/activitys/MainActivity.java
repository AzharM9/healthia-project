package com.example.firebaseapp.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.firebaseapp.R;

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

/*In this part [19]
*   -> Show user show user specific posts
*   Signed in user's post will be displayed in ProfileFragment
*   Other user's post will be displayed in ThereProfileActivity
*
*   Changes in user's list
*   1) Click any user to display having two options
*       1) chat: go to chat acitivity to chat activity with that person
*       2)Profile: see profile of that person
*   Changes in postsList
*   1) Click top of any post to show profile of the user of post
*  changes in Chat Activity
*   !) Hide Addpost icon from toolbar*/
