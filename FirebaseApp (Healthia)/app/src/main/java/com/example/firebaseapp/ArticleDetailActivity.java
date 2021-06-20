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
import com.example.firebaseapp.activitys.PostDetailActivity;
import com.example.firebaseapp.activitys.ThereProfileActivity;
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

public class ArticleDetailActivity extends AppCompatActivity {

    //to get detail of user and post
    String hisUid, myUid, myEmail, myName, myDp,
            aId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;

    //progress bar
    ProgressDialog pd;

    //views
    ImageView uPictureIv, aImageIv;
    TextView uNameTv, aTimeTv, aTitleTv, aDescriptionTv, aCommentsTv, aCategoryTv;
    ImageButton moreBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelReply> replyList;
    AdapterReply adapterReply;

    //add comments views
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        //Action bar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Article Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of the post using intent
        Intent intent = getIntent();
        aId = intent.getStringExtra("postId");

        //init views
        uPictureIv = findViewById(R.id.uPictureIv);
        aImageIv = findViewById(R.id.aImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        aTimeTv = findViewById(R.id.aTimeTv);
        aTitleTv = findViewById(R.id.aTitleTv);
        aCategoryTv = findViewById(R.id.aCategoryTv);
        aDescriptionTv = findViewById(R.id.fDescriptionTv);
        aCommentsTv = findViewById(R.id.fCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.rAvatarIv);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ArticleDetailActivity.this);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this article?");
                    //delete button
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beginDelete();
                            onBackPressed();
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
                    Intent intent = new Intent(ArticleDetailActivity.this, AddArticleActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", aId);
                    startActivity(intent);

                } else if(id == 2) {
                    /*click to go to ThereProfileActivity with uid, this uid is of clicked user
                     * which will be used to show user specific data/post*/
                    Intent intent = new Intent(ArticleDetailActivity.this, ThereProfileActivity.class);
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
                        Query fquery = FirebaseDatabase.getInstance().getReference("Articles").orderByChild("aId").equalTo(aId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                //deleted
                                Toast.makeText(com.example.firebaseapp.ArticleDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(com.example.firebaseapp.ArticleDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Articles").orderByChild("aId").equalTo(aId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                //deleted
                Toast.makeText(ArticleDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
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
                            Glide.with(ArticleDetailActivity.this).load(myDp)
                                    .placeholder(R.drawable.ic_default_img)
                                    .apply(new RequestOptions().override(40,40))
                                    .into(cAvatarIv);
                        } catch (Exception e) {
//                            Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                            Glide.with(ArticleDetailActivity.this)
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Articles");
        Query query = ref.orderByChild("aId").equalTo(aId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the posts until get the required post
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String pTitle = ""+ds.child("aTitle").getValue();
                    String aCategory = ""+ds.child("aCategory").getValue();
                    String pDescr = ""+ds.child("aDescription").getValue();
                    pLikes = ""+ds.child("aLikes").getValue();
                    String pTimeStamp = ""+ds.child("aTime").getValue();
                    pImage = ""+ds.child("aImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
//                    String commentCount = ""+ds.child("fReplies").getValue();


                    //convert timestamp to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("MMM d, yyyy hh:mm aa", calendar).toString();

                    //set data
                    aTitleTv.setText(pTitle);
                    aCategoryTv.setText(aCategory);
                    aDescriptionTv.setText(pDescr);
                    aTimeTv.setText(pTime);


                    uNameTv.setText(hisName);

                    //set post image
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage.equals("noImage")){
                        //hide imageView
                        aImageIv.setVisibility(View.GONE);
                    }
                    else{
                        //show imageView
                        aImageIv.setVisibility(View.VISIBLE);
                        try{
//                            Picasso.get().load(pImage).into(aImageIv);
                            Glide.with(ArticleDetailActivity.this)
                                    .load(pImage)
                                    .placeholder(R.drawable.ic_image_black_24)
                                    .apply(new RequestOptions().centerCrop())
                                    .into(aImageIv);
                        }
                        catch (Exception e){

                        }

                        aImageIv.setOnClickListener(v->{
                            Intent intent = new Intent(ArticleDetailActivity.this, FullScreenImageActivity.class);
                            intent.putExtra("postId", aId);
                            intent.putExtra("type", "Articles");
                            startActivity(intent);
                        });
                    }

                    //set user image in comment part
                    if (!hisDp.equals("")) {
                        try {
//                            Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                            Glide.with(ArticleDetailActivity.this).load(hisDp)
                                    .placeholder(R.drawable.ic_default_img)
                                    .apply(new RequestOptions().override(50,50))
                                    .into(uPictureIv);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                            Glide.with(ArticleDetailActivity.this)
                                    .load(R.drawable.ic_default_img)
                                    .apply(new RequestOptions().override(50,50))
                                    .into(uPictureIv);
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