package com.example.firebaseapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.firebaseapp.AdapterForum;
import com.example.firebaseapp.AddForumActivity;
import com.example.firebaseapp.MapsActivity;
import com.example.firebaseapp.ModelForum;
import com.example.firebaseapp.R;
import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.MainActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ForumFragment extends Fragment {

    private Activity mActivity;

    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    Spinner spinner;
    AutoCompleteTextView dropdown_text;
    RecyclerView recyclerView;
    List<ModelForum> forumList;
    AdapterForum adapterForum;

    ExtendedFloatingActionButton fab, maps;

    //user info
    String email, uid;


    public ForumFragment() {
        // Required empty public constructor
    }

    public static ForumFragment newInstance(String param1, String param2) {
        ForumFragment fragment = new ForumFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forum, container, false);

        ((DashboardActivity) getActivity()).getSupportActionBar().setTitle("Home");

        setHasOptionsMenu(true); //to show menu option in fragment

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //recycler view and its properties
        spinner = view.findViewById(R.id.sp_type);
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        // init
        maps = view.findViewById(R.id.maps);
        fab = view.findViewById(R.id.fab);

        //show newest posts first, for this load from from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.forum_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                String forumType = parent.getItemAtPosition(i).toString();
//                Toast.makeText(parent.getContext(), forumType, Toast.LENGTH_SHORT).show();
                switch (forumType){
                    case "All":
                        loadPosts();
                        break;
                    case "Health":
                        filterForum("Health");
                        break;
                    case "Other":
                        filterForum("Other");
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(parent.getContext(), "Zonk!", Toast.LENGTH_SHORT).show();
            }
        });

        //set layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        forumList = new ArrayList<>();

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MapsActivity.class));
            }
        });

        //handle fab click post
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddForumActivity.class));
            }
        });

        loadPosts();



        // Inflate the layout for this fragment
        return view;
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forumList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelForum modelForum = ds.getValue(ModelForum.class);

                    forumList.add(modelForum);

                    //adapter
                    adapterForum = new AdapterForum(getActivity(), forumList);

                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapterForum);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
//                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchPosts(String searchQuery){
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forumList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelForum modelForum = ds.getValue(ModelForum.class);

                    if (modelForum.getfTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelForum.getfDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        forumList.add(modelForum);

                        //adapter
                        adapterForum = new AdapterForum(getActivity(), forumList);

                        //set adapter to recyclerView
                        recyclerView.setAdapter(adapterForum);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in ca of error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterForum(String searchQuery) {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Forums");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forumList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelForum modelForum = ds.getValue(ModelForum.class);

                    if (modelForum.getfCategory().toLowerCase().contains(searchQuery.toLowerCase())){
                        forumList.add(modelForum);

                        //adapter
                        adapterForum = new AdapterForum(getActivity(), forumList);

                        //set adapter to recyclerView
                        recyclerView.setAdapter(adapterForum);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in ca of error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here
            email = user.getEmail();
            uid = user.getUid();
        }else{
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

    }


    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide about us, feedback, log out
        menu.findItem(R.id.action_about_us).setVisible(false);
        menu.findItem(R.id.action_feedback).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

        //searchView to search posts by posts title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else{
                    loadPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_chatlist){
            actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle("Chats");
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            ChatListFragment fragment6 = new ChatListFragment();
            FragmentTransaction ft6 = getActivity().getSupportFragmentManager().beginTransaction();
            ft6.replace(R.id.content, fragment6, "");
            ft6.addToBackStack(null);
            ft6.commit();
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