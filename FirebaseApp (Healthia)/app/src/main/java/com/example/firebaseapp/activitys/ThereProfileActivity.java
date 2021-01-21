package com.example.firebaseapp.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    //firebase
    FirebaseAuth firebaseAuth;
    private FirebaseUser mCurrent_user;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    //views from xml
    ImageView avatarTv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    Button mProfileSendReqBtn;

    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String otherUser_uid;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views
        avatarTv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        mProfileSendReqBtn = findViewById(R.id.mProfileSendReqBtn);
        postRecyclerView = findViewById(R.id.recyclerview_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        otherUser_uid = intent.getStringExtra("uid");

        mCurrentState = "not_friends";

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("uid").equalTo(otherUser_uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //get    data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    if (!image.equals("")) {
                        try {
                            //if image is received then set
                            Picasso.get().load(image).into(avatarTv);
                        } catch (Exception e) {
                            //if there is any exception while getting image the set default
                            Picasso.get().load(R.drawable.ic_default_img_white).into(avatarTv);
                        }
                    }
                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image the set default
                    }

                    //----------------------FRIEND LIST / REQUEST FEATURE------------------------
                    //check if currentUser has otherUser uid
                    mFriendReqDatabase.child(mCurrent_user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(otherUser_uid)) {
                                        //get value of request type and set text button according to state
                                        String req_type = dataSnapshot.child(otherUser_uid).child("request_type")
                                                .getValue().toString();

                                        if (req_type.equals("received")) {

                                            mCurrentState = "req_received";
                                            mProfileSendReqBtn.setText("Accept Friend Request");

                                        } else if (req_type.equals("sent")) {

                                            mCurrentState = "req_sent";
                                            mProfileSendReqBtn.setText("Cancel Friend Request");
                                        }
                                    }
                                    else{
                                        mFriendDatabase.child(mCurrent_user.getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        //check if exists as friend
                                                        if(dataSnapshot.hasChild(otherUser_uid)){
                                                            mCurrentState = "friends";
                                                            mProfileSendReqBtn.setText("Unfriend this Person");
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        //button for send friend req, accept friend req, & unfriend
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //disable button
                mProfileSendReqBtn.setEnabled(false);

                //---------------------NOT FRIEND STATE-------------------------------------
                if (mCurrentState.equals("not_friends")) {

                    //add data to firebase friend_req (Friend_req > currentUser uid >> otherUser uid, request_type: sent)
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(otherUser_uid)
                            .child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        //add data to firebase friend_req (Friend_req > otherUser uid >> currentUser uid, request_type: received)
                                        mFriendReqDatabase.child(otherUser_uid).child(mCurrent_user.getUid())
                                                .child("request_type").setValue("received")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mProfileSendReqBtn.setEnabled(true);
                                                        mCurrentState = "req_sent";
                                                        mProfileSendReqBtn.setText("Cancel Friend Request");

//                                        Toast.makeText(ThereProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(ThereProfileActivity.this, "Failed Sending Friend Request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                //---------------CANCEL REQUEST FRIEND REQUEST STATE--------------------------------
                if (mCurrentState.equals("req_sent")) {

                    //delete value inside currentUser obj in Friend_req tree
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(otherUser_uid).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //delete value inside otherUser obj in Friend_req tree
                                    mFriendReqDatabase.child(otherUser_uid).child(mCurrent_user.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrentState = "not_friends";
                                                    mProfileSendReqBtn.setText("Send Friend Request");

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }

                //----------------REQ RECEIVED STATE---------------------------
                if (mCurrentState.equals("req_received")) {

                    //save date currentUser becoming friend with otherUser vice versa
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid()).child(otherUser_uid)
                            .setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(otherUser_uid).child(mCurrent_user.getUid())
                                            .setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            //delete value inside currentUser obj in Friend_req tree
                                            mFriendReqDatabase.child(mCurrent_user.getUid()).child(otherUser_uid).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            //delete value inside otherUser obj in Friend_req tree
                                                            mFriendReqDatabase.child(otherUser_uid).child(mCurrent_user.getUid()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mProfileSendReqBtn.setEnabled(true);
                                                                            mCurrentState = "friends";
                                                                            mProfileSendReqBtn.setText("Unfriend this Person");

                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {

                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                }
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadHisPosts();

    }

    private void loadHisPosts() {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equal to uid of current user*/
        Query query = ref.orderByChild("uid").equalTo(otherUser_uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
//            mProfileTv.setText(user.getEmail());

        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id == R.id.action_chatlist) {
            //start chat activity with that user
            Intent intent = new Intent(ThereProfileActivity.this, ChatActivity.class);
            intent.putExtra("hisUid",otherUser_uid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}