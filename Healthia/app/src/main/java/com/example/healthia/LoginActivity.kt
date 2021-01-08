package com.example.healthia

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    //Declare an instance of FirebaseAuth
    lateinit var mAuth: FirebaseAuth

    //progress dialog
    var pd: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //actionbar and it's title
        val actionBar = supportActionBar
        actionBar?.setTitle("Login")

        //enable back button
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance()

        //login button click
        login_btn.setOnClickListener(View.OnClickListener {
            //input data
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString().trim { it <= ' ' }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //invalid email pattern set error
                emailEt.setError("Invalid Email")
                emailEt.setFocusable(true)
            } else {
                //valid email pattern
                loginUser(email, password)
            }
        })
        not_have_accountTv.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        })

        //init progress dialog
        pd = ProgressDialog(this)
        pd!!.setMessage("Logging In...")
    }

    private fun loginUser(email: String, password: String) {
        //show progress dialog
        pd?.show()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //dismiss progress dialog
                    pd?.dismiss()
                    Toast.makeText(this@LoginActivity, "Login Berhasil...",
                        Toast.LENGTH_SHORT).show()
                    // Sign in success, update UI with the signed-in user's information
                    val user: FirebaseUser? = mAuth.currentUser

                    //if user is signing in first time then get and show user info
                    if (task.result?.additionalUserInfo?.isNewUser == true){

                    }
                    //Get user email and uid from auth
                    val email = user!!.email
                    val uid = user.uid

                    //when user is registered store user info in firebase realtime database too
                    //using hashMap
                    val hashMap = HashMap<Any, String?>()
                    hashMap["email"] = email
                    hashMap["uid"] = uid
                    hashMap["name"] = ""
                    hashMap["phone"] = "" //will add later (e.g edit profile)
                    hashMap["image"] = "" //will add later (e.g edit profile)

                    //firebase database instance
                    val database = FirebaseDatabase.getInstance()

                    //path to store user data name "Users"
                    val reference = database.getReference("Users")

                    //put data within hashmap in database
                    reference.child(uid).setValue(hashMap)
                    //user is logged in, so start MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    //dismiss progress dialog
                    pd?.dismiss()
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e -> //error, get and show error message
                Toast.makeText(this@LoginActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go previous activity
        return super.onSupportNavigateUp()
    }
}