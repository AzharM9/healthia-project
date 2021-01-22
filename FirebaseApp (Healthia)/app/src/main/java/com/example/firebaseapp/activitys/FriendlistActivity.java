package com.example.firebaseapp.activitys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendlistActivity extends AppCompatActivity {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    Context context;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        context = FriendlistActivity.this;

        mFriendsList = findViewById(R.id.friends_recyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //set actionbar title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Friend list");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(FriendsViewHolder friendsViewHolder, Friends friends, int i) {
                friendsViewHolder.setDate("Friend since "+friends.getDate());

                String list_user_id = getRef(i).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //getData
                        String userName = snapshot.child("name").getValue().toString();
                        String userImage = snapshot.child("image").getValue().toString();

                        //setData
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setImage(userImage);

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                                            intent.putExtra("uid", list_user_id);
                                            context.startActivity(intent);
                                        }
                                        if(which == 1){
                                            //chat clicked
                                            Intent intent = new Intent(context,ChatActivity.class);
                                            intent.putExtra("hisUid", list_user_id);
                                            context.startActivity(intent);
                                        }
                                    }
                                });
                                builder.create().show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setDate(String date){
            TextView userNameView = itemView.findViewById(R.id.user_single_status);
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = itemView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setImage(String image) {
            CircleImageView userImageView = itemView.findViewById(R.id.user_single_image);
            if (image.equals("")){

                try{
                    Picasso.get().load(R.drawable.ic_default_img).placeholder(R.drawable.ic_default_img).into(userImageView);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
            else{
                try{
                    Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(userImageView);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}