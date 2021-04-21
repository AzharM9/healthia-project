package com.example.firebaseapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebaseapp.activitys.ChatActivity;
import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.AddNewFriendsActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.activitys.ThereProfileActivity;
import com.example.firebaseapp.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendListFragment extends Fragment {

    private Activity mActivity;

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    Context context;
    ActionBar actionBar;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendListFragment newInstance(String param1, String param2) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friendlist, container, false);

        context = getActivity();

        mFriendsList = view.findViewById(R.id.friends_recyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerAdapter<Friends, FriendListFragment.FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendListFragment.FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendListFragment.FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(FriendListFragment.FriendsViewHolder friendsViewHolder, Friends friends, int i) {
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
                                            Intent intent = new Intent(context, ChatActivity.class);
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

        //set actionbar title
        actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Friend list");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(context));

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
//                    Picasso.get().load(R.drawable.ic_default_img).placeholder(R.drawable.ic_default_img).into(userImageView);
                    Glide.with(mView).load(R.drawable.ic_default_img).placeholder(R.drawable.ic_default_img).into(userImageView);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
            else{
                try{
//                    Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(userImageView);
                    Glide.with(mView).load(image).placeholder(R.drawable.ic_default_img).into(userImageView);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_friendlist, menu);

        menu.findItem(R.id.action_addfriends);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();

        int id = item.getItemId();
        if (id == R.id.action_addfriends){
            Intent intent = new Intent(getActivity(), AddNewFriendsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

}