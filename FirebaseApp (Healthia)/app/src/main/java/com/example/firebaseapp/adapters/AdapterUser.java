package com.example.firebaseapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseapp.activitys.ChatActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.activitys.ThereProfileActivity;
import com.example.firebaseapp.models.ModelUser;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUser> userList;

    FirebaseAuth firebaseAuth;
    private FirebaseUser mCurrent_user;

    private DatabaseReference mRootRef;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private String mCurrentState;
    String otherUser_uid;

    Button mSendReqBtn, mDeclineBtn;

    //constructor
    public AdapterUser(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //inflate layout(item_row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_users, viewGroup, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

//        otherUser_uid = hisUID;
        mCurrentState = "not_friends";

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        //set data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);

        if(userImage.equals("")){
            holder.mAvatarIv.setImageResource(R.drawable.ic_default_img);
        }
        else{
            try {
//                Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);
                Glide.with(context).load(userImage)
                        .placeholder(R.drawable.ic_default_img)
//                        .apply(new RequestOptions().override(70,70))
                        .into(holder.mAvatarIv);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        //handle item click
        holder.itemView.setOnClickListener(v -> {

            //show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    if (which == 0){
                        //profile clicked
                        /*click to go to ThereProfileActivity with uid, this uid is of clicked user
                         * which will be used to show user specific data/post*/
                        Intent intent = new Intent(context, ThereProfileActivity.class);
                        intent.putExtra("uid", hisUID);
                        context.startActivity(intent);
                    }
                    if(which == 1){
                        //chat clicked
                        Intent intent = new Intent(context,ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                    }
                }
            });
            builder.create().show();
        });

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("uid").equalTo(hisUID);
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

                    //----------------------FRIEND LIST / REQUEST FEATURE------------------------
                    //check if currentUser has otherUser uid
                    mFriendReqDatabase.child(mCurrent_user.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(hisUID)) {
                                        //get value of request type and set text button according to state
                                        String req_type = dataSnapshot.child(hisUID).child("request_type")
                                                .getValue().toString();

                                        if (req_type.equals("received")) {

                                            mCurrentState = "req_received";
                                            mSendReqBtn.setText("Accept Friend Request");
                                            mDeclineBtn.setVisibility(View.VISIBLE);

                                        } else if (req_type.equals("sent")) {

                                            mCurrentState = "req_sent";
                                            mSendReqBtn.setText("Cancel Friend Request");

                                            mDeclineBtn.setVisibility(View.GONE);
                                            mDeclineBtn.setEnabled(false);
                                        }
                                    }
                                    else{
                                        //check if already friend
                                        mFriendDatabase.child(mCurrent_user.getUid())
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        //check if exists as friend
                                                        if(dataSnapshot.hasChild(hisUID)){
                                                            mCurrentState = "friends";
                                                            mSendReqBtn.setText("Unfriend this Person");

                                                            mDeclineBtn.setVisibility(View.GONE);
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
        //button for send friend req, accept friend req, & unfriend
        mSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //disable button
                mSendReqBtn.setEnabled(false);

                //---------------------NOT FRIEND STATE-------------------------------------
                if (mCurrentState.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(hisUID).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + hisUID + "/request_type", "sent");
                    requestMap.put("Friend_req/" + hisUID + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + hisUID + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error != null){
                                Toast.makeText(context, "There is some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mSendReqBtn.setEnabled(true);

                            mCurrentState = "req_sent";
                            mSendReqBtn.setText("Cancel Friend Request");
                        }
                    });
                }

                //---------------CANCEL REQUEST FRIEND REQUEST STATE--------------------------------
                if (mCurrentState.equals("req_sent")) {

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + hisUID + "/request_type", null);
                    requestMap.put("Friend_req/" + hisUID + "/" + mCurrent_user.getUid() + "/request_type", null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null){
                                Toast.makeText(context, "There is some error in canceling request", Toast.LENGTH_SHORT).show();
                            }
                            mSendReqBtn.setEnabled(true);

                            mCurrentState = "not_friends";
                            mSendReqBtn.setText("Send Friend Request");
                        }
                    });
                }

                //----------------ACCEPT REQ RECEIVED STATE---------------------------
                if (mCurrentState.equals("req_received")) {

                    //save date currentUser becoming friend with otherUser vice versa
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + hisUID + "/date", currentDate);
                    friendsMap.put("Friends/" + hisUID + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + hisUID, null);
                    friendsMap.put("Friend_req/" + hisUID + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error == null){

                                mSendReqBtn.setEnabled(true);
                                mCurrentState = "friends";
                                mSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.GONE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else{
                                String dbError = error.getMessage();
                                Toast.makeText(context, dbError, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

                //----------------UNFRIENDS-------------------------------------------
                if (mCurrentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + hisUID + "/date", null);
                    unfriendMap.put("Friends/" + hisUID + "/" + mCurrent_user.getUid() + "/date", null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            mCurrentState = "not_friends";
                            mSendReqBtn.setText("Send Friend Request");
                            mSendReqBtn.setEnabled(true);

                            mDeclineBtn.setVisibility(View.GONE);
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
                mDeclineBtn.setVisibility(View.GONE);
                //----------------DECLINE FRIEND REQUEST-----------------------------
                if (mCurrentState.equals("req_received")) {

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + hisUID + "/request_type", null);
                    requestMap.put("Friend_req/" + hisUID + "/" + mCurrent_user.getUid() + "/request_type", null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null){
                                Toast.makeText(context, "There is some error in declining request", Toast.LENGTH_SHORT).show();
                            }

                            mCurrentState = "not_friends";
                            mSendReqBtn.setEnabled(true);
                            mSendReqBtn.setText("Send Friend Request");
                            mSendReqBtn.setVisibility(View.VISIBLE);

                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView mAvatarIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            mSendReqBtn = itemView.findViewById(R.id.mSendReqBtn);
            mDeclineBtn = itemView.findViewById(R.id.mDeclineBtn);

        }
    }
}
