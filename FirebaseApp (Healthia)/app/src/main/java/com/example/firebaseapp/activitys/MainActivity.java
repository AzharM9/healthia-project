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
                finish();
            }
        });

        //handle login button click
       mLoginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               //start Login Activity
               startActivity(new Intent(MainActivity.this, LoginActivity.class));
               finish();
            }
       });
    }
}

