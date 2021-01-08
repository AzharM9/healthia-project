package com.example.healthia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login_or_register.*

class LoginOrRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_or_register)

        register_btn.setOnClickListener ( View.OnClickListener {
            startActivity(Intent(this@LoginOrRegisterActivity, RegisterActivity::class.java))
        })

        login_btn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@LoginOrRegisterActivity, LoginActivity::class.java))
        })
    }
}