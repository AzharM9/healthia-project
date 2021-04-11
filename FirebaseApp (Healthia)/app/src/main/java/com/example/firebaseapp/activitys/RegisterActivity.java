package com.example.firebaseapp.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    TextInputEditText mNameEt, mEmailEt, mPasswordEt, mPhoneEt;
    EditText mRegisterEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    String role;
    Spinner mUserType;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbar and it's title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mNameEt = findViewById(R.id.nameEt);
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mPhoneEt = findViewById(R.id.phoneEt);
        mUserType = findViewById(R.id.userTypeEt);
        mRegisterEt = findViewById(R.id.registerEt);
        mRegisterBtn = findViewById(R.id.register_btn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //in onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(RegisterActivity.this,
                        R.array.user_type,R.layout.support_simple_spinner_dropdown_item);
        mUserType.setAdapter(adapter);

        mUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String userRole = adapterView.getItemAtPosition(i).toString();
                switch (userRole){
                    case "Patient":
                        mRegisterEt.setVisibility(View.GONE);
                        mRegisterEt.setText("0000000000000000");
                        role = userRole;
                        break;
                    case "Doctor":
                        mRegisterEt.setVisibility(View.VISIBLE);
                        mRegisterEt.setText("");
                        role = userRole;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                mUserType.setError("Please select either patient/doctor");
            }
        });
        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input email password
                String name = mNameEt.getText().toString().trim();
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String phone = mPhoneEt.getText().toString().trim();
                String registerNum = mRegisterEt.getText().toString().trim();

                //validate
                if (name.length() < 3 || name.length()> 50){
                    mNameEt.setError("Name length at least 3 characters");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edit text
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);

                }
                else if (password.length() < 6){
                    //set error and focus to password edit text
                    mPasswordEt.setError("Password length at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }
                else if (!Patterns.PHONE.matcher(phone).matches()||phone.length() < 11 || phone.length() > 12){
                    mPhoneEt.setError("Phone number length at least 11 digits and not including characters");
                }
                else if (!Patterns.PHONE.matcher(registerNum).matches()
                        || registerNum.length() < 16 || registerNum.length() > 16){
                    mRegisterEt.setError("must 16 digits");
                }
                else{
                    registerUser(name , email, password, phone, role, registerNum); //register the user
                }
            }
        });
        //handle login textView click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String name, String email, String password, String phone, String userRole, String registerNum) {
        //email and password pattern is valid, show progress dialog start registering them
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, dismiss dialog start register Activity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();

                            //Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            //when user is registered store user info in firebase realtime database too
                            //using hashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("device_token", deviceToken);
                            hashMap.put("phone", phone); //will add later (e.g edit profile)
                            hashMap.put("role", userRole); // user role (Patient/Doctor)
                            hashMap.put("regisNum", registerNum); //Doctor's STR number
                            hashMap.put("image", ""); //will add later (e.g edit profile)
                            hashMap.put("cover", ""); //will add later (e.g edit profile)

                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            //path to store user data name "Users"
                            DatabaseReference reference = database.getReference("Users");

                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progress dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go previous activity
        return super.onSupportNavigateUp();
    }
}

/*Testing
* 1. Run project
* 2. Enter Invalid email pattern e.g without @, .com, etc
* 3. enter empty password or less than 6 characters
* 4. enter valid email/password
* 5. enter existing(registered) email (it should not be accepted)
* 6. check registered user in firebase*/