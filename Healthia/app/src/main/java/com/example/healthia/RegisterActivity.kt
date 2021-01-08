package com.example.healthia

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {

    var progressDialog: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //actionbar and it's title
        val actionBar = supportActionBar
        actionBar!!.setTitle("Create Account")

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Registering user...")

        //in onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance()

        //handle register btn click
        register_btn.setOnClickListener(View.OnClickListener {
            //input email password
            val email = emailEt.getText().toString().trim { it <= ' ' }
            val password = passwordEt.getText().toString().trim { it <= ' ' }

            //validate
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //set error and focus to email edit text
                emailEt.setError("Invalid Email")
                emailEt.setFocusable(true)
            } else if (password.length < 6) {
                //set error and focus to password edit text
                passwordEt.setError("Password length at least 6 characters")
                passwordEt.setFocusable(true)
            } else {
                registerUser(email, password) //register the user
            }
        })
        //handle login textView click listener
        have_accountTv.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@RegisterActivity,
                    LoginActivity::class.java
                )
            )
        })
    }

    private fun registerUser(email: String, password: String) {
        //email and password pattern is valid, show progress dialog start registering them
        progressDialog!!.show()
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, dismiss dialog start register Activity
                    progressDialog!!.dismiss()
                    val user = mAuth!!.currentUser

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
                    Toast.makeText(
                        this@RegisterActivity,
                        """Registered...${user!!.email}""".trimIndent(),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    progressDialog!!.dismiss()
                    Toast.makeText(
                        this@RegisterActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e -> //error, dismiss progress dialog and get and show the error message
                progressDialog!!.dismiss()
                Toast.makeText(this@RegisterActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go previous activity
        return super.onSupportNavigateUp()
    }
}