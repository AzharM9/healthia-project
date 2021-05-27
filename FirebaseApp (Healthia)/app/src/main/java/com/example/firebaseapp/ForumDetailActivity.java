package com.example.firebaseapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseapp.activitys.MainActivity;
import com.example.firebaseapp.activitys.ThereProfileActivity;
import com.example.firebaseapp.notifications.APIService;
import com.example.firebaseapp.notifications.Client;
import com.example.firebaseapp.notifications.Data;
import com.example.firebaseapp.notifications.Response;
import com.example.firebaseapp.notifications.Sender;
import com.example.firebaseapp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

public class ForumDetailActivity extends AppCompatActivity {

    //to get detail of user and post
    String hisUid, myUid, myEmail, myName, myDp,
            fId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //progress bar
    ProgressDialog pd;

    //views
    ImageView uPictureIv, fImageIv;
    TextView uNameTv, fTimeTv, fTitleTv, fDescriptionTv, pLikesTv, fCommentsTv, fCategoryTv;
    ImageButton moreBtn;
//    Button likeBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelReply> replyList;
    AdapterReply adapterReply;

    //add comments views
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_detail);

        //Action bar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Forum Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of the post using intent
        Intent intent = getIntent();
        fId = intent.getStringExtra("postId");

        //init views
        uPictureIv = findViewById(R.id.uPictureIv);
        fImageIv = findViewById(R.id.fImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        fTimeTv = findViewById(R.id.fTimeTv);
        fTitleTv = findViewById(R.id.fTitleTv);
        fCategoryTv = findViewById(R.id.fCategoryTv);
        fDescriptionTv = findViewById(R.id.fDescriptionTv);
//        pLikesTv = findViewById(R.id.pLikesTv);
        fCommentsTv = findViewById(R.id.fCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
//        likeBtn = findViewById(R.id.likeBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.rAvatarIv);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

//        setLikes();

        //set subtitle of action bar
        //actionBar.setSubtitle("SignedIn as: "+myEmail);

        loadComments();

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postReply();
            }
        });

        //like button click handle
//        likeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                likePost();
//            }
//        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
    }

    private void addToHisNotifications(String hisUid, String fId, String notification) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String > hashMap = new HashMap<>();
        hashMap.put("fId", fId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("fUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        sendPushNotification(hisUid, "New Forum Reply", myName, "replied on your forum");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private void sendPushNotification(final String hisUid, String title, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            ""+myUid,
                            name+" "+message,
                            ""+title,
                            ""+hisUid,
                            ""+fId,
                            R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

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

    private void loadComments() {
        //layout(linear) for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //init comment list
        replyList = new ArrayList<>();

        //Path of the post, to get it's comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums").child(fId).child("Replies");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                replyList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelReply modelReply = ds.getValue(ModelReply.class);

                    replyList.add(modelReply);

                    //setup adapter
                    adapterReply = new AdapterReply(getApplicationContext(), replyList, myUid, fId);

                    //set adapter
                    recyclerView.setAdapter(adapterReply);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        //creating popup menu currently having option Delete, we will add more option later
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //show delete option  in only post(s) of currently signed-in user
        //add items in menu
        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        } else {
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Visit Profile");
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    //delete is clicked
                    //show delete message confirm dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ForumDetailActivity.this);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this forum?");
                    //delete button
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beginDelete();
                        }
                    });
                    //cancel delete button
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // create and show dialog
                    builder.create().show();
                }
                else if(id == 1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(ForumDetailActivity.this, AddForumActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", fId);
                    startActivity(intent);

                } else if(id == 2) {
                    /*click to go to ThereProfileActivity with uid, this uid is of clicked user
                     * which will be used to show user specific data/post*/
                    Intent intent = new Intent(ForumDetailActivity.this, ThereProfileActivity.class);
                    intent.putExtra("uid", hisUid);
                    startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image

        if(pImage.equals("noImage")){

            //post is without image
            deleteWithoutImage();
        }
        else{

            //post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        /*Steps:
         * 1) Delete Image using url
         * 2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Forums").orderByChild("fId").equalTo(fId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                //deleted
                                Toast.makeText(ForumDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, can't go further
                        pd.dismiss();
                        Toast.makeText(ForumDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Forums").orderByChild("fId").equalTo(fId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                //deleted
                Toast.makeText(ForumDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void setLikes() {
//        //when the details of post is loading, also check if current user has like it or not
//        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
//        likesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child(postId).hasChild(myUid)){
//                    //user has liked this post
//                    /*To indicate that the post is liked by this (SignedIn) user
//                     * Change drawable left icon of like button
//                     * Change text of like button from "Like" to "Liked"*/
////                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
////                    likeBtn.setText("Liked");
//                }
//                else{
//                    //user has not liked this post
//                    /*To indicate that the post is not liked by this (SignedIn) user
//                     * Change drawable left icon of like button
//                     * Change text of like button from "Liked" to "Like"*/
////                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
////                    likeBtn.setText("Like");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void likePost() {
//        //get total number of likes for the post, whose like like button clicked
//        //if currently signed in user has not liked it before
//        //increase value by 1, otherwise decrease value by 1
//        mProcessLike = true;
//        //get id of the post clicked
//        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
//        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
//        likesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (mProcessLike){
//                    if (dataSnapshot.child(postId).hasChild(myUid)){
//                        //already liked, so remove like
//                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
//                        likesRef.child(postId).child(myUid).removeValue();
//                        mProcessLike = false;
//
//                    }
//                    else{
//                        //not liked, like it
//                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
//                        likesRef.child(postId).child(myUid).setValue("Liked");
//                        mProcessLike = false;
//
//                        addToHisNotifications(""+hisUid, ""+postId, "Liked your post");
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void postReply() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding Reply...");

        //get data from comment edit text
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)){
            //no value is entered
            Toast.makeText(this, "Reply is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //each post will have a child "Comments" that will contain comment of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums").child(fId).child("Replies");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put into in hashMap
        hashMap.put("rId", timeStamp);
        hashMap.put("reply", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        //put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added
                        pd.dismiss();
                        Toast.makeText(ForumDetailActivity.this, "Reply added...", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateReplyCount();
                        if (!hisUid.equals(myUid)) addToHisNotifications(""+hisUid, ""+ fId, "Replied on your forum");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed not added
                        pd.dismiss();
                        Toast.makeText(ForumDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateReplyCount() {
        //whenever user adds comment increase the comment count as we did for like count
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums").child(fId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment) {
                    String comment = ""+ snapshot.child("fReplies").getValue();
                    int newCommentVal = Integer.parseInt(comment) + 1;
                    ref.child("fReplies").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    if (!myDp.equals("")) {
                        try {
                            //if image is received then set
//                            Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                            Glide.with(ForumDetailActivity.this).load(myDp)
                                    .placeholder(R.drawable.ic_default_img)
                                    .apply(new RequestOptions().override(40,40))
                                    .into(cAvatarIv);
                        } catch (Exception e) {
//                            Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                            Glide.with(ForumDetailActivity.this)
                                    .load(R.drawable.ic_default_img)
                                    .apply(new RequestOptions().override(40,40))
                                    .into(cAvatarIv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //get post using id of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums");
        Query query = ref.orderByChild("fId").equalTo(fId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the posts until get the required post
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String pTitle = ""+ds.child("fTitle").getValue();
                    String fCategory = ""+ds.child("fCategory").getValue();
                    String pDescr = ""+ds.child("fDescription").getValue();
//                    pLikes = ""+ds.child("fLikes").getValue();
                    String pTimeStamp = ""+ds.child("fTime").getValue();
                    pImage = ""+ds.child("fImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("fReplies").getValue();


                    //convert timestamp to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("MMM d, yyyy hh:mm aa", calendar).toString();

                    //set data
                    fTitleTv.setText(pTitle);
                    fCategoryTv.setText(fCategory);
                    fDescriptionTv.setText(pDescr);
//                    pLikesTv.setText(pLikes + "Likes");
                    fTimeTv.setText(pTime);
//                    fCommentsTv.setText(commentCount+ " Comments");

                    uNameTv.setText(hisName);

                    //set post image
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage.equals("noImage")){
                        //hide imageView
                        fImageIv.setVisibility(View.GONE);
                    }
                    else{
                        //show imageView
                        fImageIv.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImage).into(fImageIv);
                        }
                        catch (Exception e){

                        }
                    }

                    //set user image in comment part
                    if (!hisDp.equals("")) {
                        try {
                            Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //user is signed in
            //myEmail = user.getEmail();
            myUid = user.getUid();
        } else {
            //user not signed in, go to Main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
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

}