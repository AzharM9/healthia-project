package com.example.firebaseapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.MainActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterChatlist;
import com.example.firebaseapp.models.ModelChat;
import com.example.firebaseapp.models.ModelChatlist;
import com.example.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    private Activity mActivity;

    //firebase Auth
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUser> userList;
    DatabaseReference refence;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    ActionBar actionBar;


    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        // init
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();

        refence = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        refence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        checkUserStatus();

        actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void loadChats() {
        userList = new ArrayList<>();
        refence = FirebaseDatabase.getInstance().getReference("Users");
        refence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatlist chatlist: chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId()) ) {
                            userList.add(user);
                            break;
                        }
                    }
                    // adapter
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    // set adapter
                    recyclerView.setAdapter(adapterChatlist);
                    // set last message
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                boolean theSeenMessage = true;
                String sender = currentUser.getUid();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null){
                        continue;
                    }
                    sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(uid) ||
                            chat.getReceiver().equals(uid) && chat.getSender().equals(currentUser.getUid())){
                        theLastMessage = chat.getMessage();
                        theSeenMessage = chat.isSeen();
                    }
                }
                adapterChatlist.setLastMessageMap(uid, theLastMessage);
                adapterChatlist.setSeenMessageMap(uid, theSeenMessage, sender);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here
            //set email of logged in user
//            mProfileTv.setText(user.getEmail());
        }else{
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onStop() {
        actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        super.onStop();
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