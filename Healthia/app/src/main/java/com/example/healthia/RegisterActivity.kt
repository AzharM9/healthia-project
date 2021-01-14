package com.example.healthia

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.healthia.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegisterActivity : AppCompatActivity() {
    //views
    lateinit var mEmailEt: EditText
    lateinit var mPasswordEt: EditText
    lateinit var mRegisterBtn: Button
    lateinit var mHaveAccountTv: TextView

    //progressbar to display while registering user
    var progressDialog: ProgressDialog? = null

    //Declare an instance of FirebaseAuth
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

        //init
        mEmailEt = findViewById(R.id.emailEt)
        mPasswordEt = findViewById(R.id.passwordEt)
        mRegisterBtn = findViewById(R.id.register_btn)
        mHaveAccountTv = findViewById(R.id.have_accountTv)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Registering user...")

        //in onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance()

        //handle register btn click
        mRegisterBtn.setOnClickListener(View.OnClickListener {
            //input email password
            val email = mEmailEt.getText().toString().trim { it <= ' ' }
            val password = mPasswordEt.getText().toString().trim { it <= ' ' }

            //validate
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //set error and focus to email edit text
                mEmailEt.setError("Invalid Email")
                mEmailEt.setFocusable(true)
            } else if (password.length < 6) {
                //set error and focus to password edit text
                mPasswordEt.setError("Password length at least 6 characters")
                mPasswordEt.setFocusable(true)
            } else {
                registerUser(email, password) //register the user
            }
        })
        //handle login textView click listener
        mHaveAccountTv.setOnClickListener(View.OnClickListener { startActivity(Intent(this@RegisterActivity, LoginActivity::class.java)) })
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
                        hashMap["cover"] = "" //will add later (e.g edit profile)

                        //firebase database instance
                        val database = FirebaseDatabase.getInstance()

                        //path to store user data name "Users"
                        val reference = database.getReference("Users")

                        //put data within hashmap in database
                        reference.child(uid).setValue(hashMap)
                        Toast.makeText(this@RegisterActivity, """
     Registered...
     ${user.email}
     """.trimIndent(), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog!!.dismiss()
                        Toast.makeText(this@RegisterActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e -> //error, dismiss progress dialog and get and show the error message
                    progressDialog!!.dismiss()
                    Toast.makeText(this@RegisterActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go previous activity
        return super.onSupportNavigateUp()
    }
}