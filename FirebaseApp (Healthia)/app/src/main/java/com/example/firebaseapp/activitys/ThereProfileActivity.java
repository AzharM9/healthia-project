package com.example.firebaseapp.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseapp.ForumDetailActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.example.firebaseapp.notifications.APIService;
import com.example.firebaseapp.notifications.Client;
import com.example.firebaseapp.notifications.Data;
import com.example.firebaseapp.notifications.Response;
import com.example.firebaseapp.notifications.Sender;
import com.example.firebaseapp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThereProfileActivity extends AppCompatActivity {

    //firebase
    FirebaseAuth firebaseAuth;
    private FirebaseUser mCurrent_user;

    private DatabaseReference mRootRef;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    //views from xml
    CardView cardView, doctorTagCv;
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv, ageTv, weightTv, heightTv;
    Button mProfileSendReqBtn, mDeclineBtn;
    ConstraintLayout personal_info_layout;

    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String otherUser_uid, myName;

    private String mCurrentState;

    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views
        cardView = findViewById(R.id.cardView);
        doctorTagCv = findViewById(R.id.doctorTag);
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        ageTv = findViewById(R.id.ageTv);
        weightTv = findViewById(R.id.weightTv);
        heightTv = findViewById(R.id.heightTv);
        personal_info_layout = findViewById(R.id.personalInfo);
        mProfileSendReqBtn = findViewById(R.id.mProfileSendReqBtn);
        mDeclineBtn = findViewById(R.id.mProfileDeclineBtn);
        postRecyclerView = findViewById(R.id.recyclerview_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

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
                    String uid = "" + ds.child("uid").getValue();
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String hideData = "" + ds.child("hideData").getValue();
                    String role = ""+ds.child("role").getValue();

                    if (role.equals("Doctor")) {
                        doctorTagCv.setVisibility(View.VISIBLE);
                    }else doctorTagCv.setVisibility(View.GONE);
                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    if (!image.equals("")) {
                        try {
                            //if image is received then set
//                            Picasso.get().load(image).into(avatarIv);
                            Glide.with(getApplicationContext())
                                    .load(image)
                                    .placeholder(R.drawable.ic_default_img_white)
                                    .apply(new RequestOptions().override(180, 180))
                                    .into(avatarIv);
                        } catch (Exception e) {
                            //if there is any exception while getting image the set default
//                            Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.ic_default_img_white)
                                    .apply(new RequestOptions().override(120, 120))
                                    .into(avatarIv);
                        }
                    }
                    try {
                        //if image is received then set
//                        Picasso.get().load(cover).into(coverIv);
                        Glide.with(getApplicationContext())
                                .load(cover)
                                .apply(new RequestOptions().centerCrop())
                                .into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image the set default
                    }

                    if (hideData.equals("false")) {
                        String age = "" + ds.child("age").getValue();
                        String weight = "" + ds.child("weight").getValue();
                        String height = "" + ds.child("height").getValue();

                        ageTv.setText(age + " years");
                        weightTv.setText(weight + " kg");
                        heightTv.setText(height + " cm");
                        personal_info_layout.setVisibility(View.VISIBLE);
                    } else {
                        personal_info_layout.setVisibility(View.GONE);
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
                                            mDeclineBtn.setVisibility(View.VISIBLE);

                                        } else if (req_type.equals("sent")) {

                                            mCurrentState = "req_sent";
                                            mProfileSendReqBtn.setText("Cancel Friend Request");

                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);
                                        }
                                    } else {
                                        //check if already friend
                                        mFriendDatabase.child(mCurrent_user.getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        //check if exists as friend
                                                        if (dataSnapshot.hasChild(otherUser_uid)) {
                                                            mCurrentState = "friends";
                                                            mProfileSendReqBtn.setText("Unfriend this Person");

                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                            mDeclineBtn.setEnabled(false);
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

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
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

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(otherUser_uid).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + otherUser_uid + "/request_type", "sent");
                    requestMap.put("Friend_req/" + otherUser_uid + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + otherUser_uid + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ThereProfileActivity.this, "There is some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);

                            addToHisNotifications("" + otherUser_uid, "Sending Friend Request");

                            mCurrentState = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                        }
                    });
                }

                //---------------CANCEL REQUEST FRIEND REQUEST STATE--------------------------------
                if (mCurrentState.equals("req_sent")) {

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + otherUser_uid + "/request_type", null);
                    requestMap.put("Friend_req/" + otherUser_uid + "/" + mCurrent_user.getUid() + "/request_type", null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ThereProfileActivity.this, "There is some error in canceling request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);

                            mCurrentState = "not_friends";
                            mProfileSendReqBtn.setText("Send Friend Request");
                        }
                    });
                }

                //----------------ACCEPT REQ RECEIVED STATE---------------------------
                if (mCurrentState.equals("req_received")) {

                    //save date currentUser becoming friend with otherUser vice versa
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + otherUser_uid + "/date", currentDate);
                    friendsMap.put("Friends/" + otherUser_uid + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + otherUser_uid, null);
                    friendsMap.put("Friend_req/" + otherUser_uid + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            } else {
                                String dbError = error.getMessage();
//                                Toast.makeText(ThereProfileActivity.this, dbError, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

                //----------------UNFRIENDS-------------------------------------------
                if (mCurrentState.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + otherUser_uid + "/date", null);
                    unfriendMap.put("Friends/" + otherUser_uid + "/" + mCurrent_user.getUid() + "/date", null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            mCurrentState = "not_friends";
                            mProfileSendReqBtn.setText("Send Friend Request");
                            mProfileSendReqBtn.setEnabled(true);

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);
                        }
                    });
                }
            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDeclineBtn.setEnabled(false);
                mDeclineBtn.setVisibility(View.INVISIBLE);
                //----------------DECLINE FRIEND REQUEST-----------------------------
                if (mCurrentState.equals("req_received")) {

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + otherUser_uid + "/request_type", null);
                    requestMap.put("Friend_req/" + otherUser_uid + "/" + mCurrent_user.getUid() + "/request_type", null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ThereProfileActivity.this, "There is some error in declining request", Toast.LENGTH_SHORT).show();
                            }

                            mCurrentState = "not_friends";
                            mProfileSendReqBtn.setEnabled(true);
                            mProfileSendReqBtn.setText("Send Friend Request");
                            mProfileSendReqBtn.setVisibility(View.VISIBLE);

                        }
                    });
                }
            }
        });

        postList = new ArrayList<>();
        loadUserInfo();
        checkUserStatus();
        loadHisPosts();

    }

    private void loadUserInfo() {
        //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    myName = ""+ds.child("name").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
//                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToHisNotifications(String hisUid, String notification) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", mCurrent_user.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        sendPushNotification(
                                "" + hisUid,
                                "" + myName,
                                "sending Friend Request");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private void sendPushNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            "" + mCurrent_user.getUid(),
                            name + " " + message,
                            "New Friend Request",
                            "" + hisUid,
                            "" + mCurrent_user.getUid(),
                            R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(mCurrent_user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of online status of current user
        dbRef.updateChildren(hashMap);
    }


    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
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
        menu.findItem(R.id.action_nearby_clinic).setVisible(false);
        menu.findItem(R.id.action_about_us).setVisible(false);
        menu.findItem(R.id.action_feedback).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
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
            intent.putExtra("hisUid", otherUser_uid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}