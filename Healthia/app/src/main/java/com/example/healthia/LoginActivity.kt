package com.example.healthia

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.healthia.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class LoginActivity : AppCompatActivity() {
    //views
    lateinit var mEmailEt: EditText
    lateinit var mPasswordEt: EditText
    lateinit var notHaveAccountTv: TextView
    lateinit var mLoginBtn: Button

    //Declare an instance of FirebaseAuth
    private var mAuth: FirebaseAuth? = null

    //progress dialog
    var pd: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //actionbar and it's title
        val actionBar = supportActionBar
        actionBar!!.setTitle("Login")

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance()

        //init
        mEmailEt = findViewById(R.id.emailEt)
        mPasswordEt = findViewById(R.id.passwordEt)
        notHaveAccountTv = findViewById(R.id.not_have_accountTv)
        mLoginBtn = findViewById(R.id.login_btn)

        //login button click
        mLoginBtn.setOnClickListener(View.OnClickListener {
            //input data
            val email = mEmailEt.getText().toString()
            val password = mPasswordEt.getText().toString().trim { it <= ' ' }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //invalid email pattern set error
                mEmailEt.setError("Invalid Email")
                mEmailEt.setFocusable(true)
            } else {
                //valid email pattern
                loginUser(email, password)
            }
        })
        notHaveAccountTv.setOnClickListener(View.OnClickListener { startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) })

        //init progress dialog
        pd = ProgressDialog(this)
        pd!!.setMessage("Logging In...")
    }

    private fun loginUser(email: String, password: String) {
        //show progress dialog
        pd!!.show()
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //dismiss progress dialog
                        pd!!.dismiss()
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser

                        //if user is signing in first time then get and show user info
                        if (task.result!!.additionalUserInfo!!.isNewUser) {

                            //Get user email and uid from auth
                            val email = user!!.email
                            val uid = user.uid

                            //when user is registered store user info in firebase realtime database too
                            //using hashMap
                            val hashMap = HashMap<Any, String?>()
                            hashMap["email"] = email
                            hashMap["uid"] = uid
                            hashMap["name"] = ""
                            hashMap["onlineStatus"] = "online"
                            hashMap["phone"] = ""
                            hashMap["image"] = ""
                            hashMap["cover"] = ""


                            //firebase database instance
                            val database = FirebaseDatabase.getInstance()

                            //path to store user data name "Users"
                            val reference = database.getReference("Users")

                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap)
                        }

                        //user is logged in, so start ProfileActivity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        //dismiss progress dialog
                        pd!!.dismiss()
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