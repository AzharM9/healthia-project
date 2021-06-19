package com.example.firebaseapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebaseapp.Constants;
import com.example.firebaseapp.FetchAddressIntentService;
import com.example.firebaseapp.MapsActivity;
import com.example.firebaseapp.activitys.AddPostActivity;
import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.MainActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.example.firebaseapp.notifications.APIService;
import com.example.firebaseapp.notifications.Client;
import com.example.firebaseapp.notifications.Data;
import com.example.firebaseapp.notifications.Response;
import com.example.firebaseapp.notifications.Sender;
import com.example.firebaseapp.notifications.Token;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FeedsFragment extends Fragment {

    private Activity mActivity;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    ProgressDialog pd;

    CircleImageView avatarIv;
    TextView createPostTv;
    FloatingActionButton panicBtn;

    //user info
    String myName, myDp, email, uid;
    ArrayList<String> list_user_id;
    String[] locationPermission;
    APIService apiService;

    private ResultReceiver resultReceiver;

    public FeedsFragment() {
        // Required empty public constructor
    }

    public static FeedsFragment newInstance(String param1, String param2) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);

        ((DashboardActivity) getActivity()).getSupportActionBar().setTitle("Home");



        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        // init
        pd = new ProgressDialog(getActivity());
        panicBtn = view.findViewById(R.id.panic_btn);
        avatarIv = view.findViewById(R.id.avatarIv);
        createPostTv = view.findViewById(R.id.createPostTv);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        resultReceiver = new AddressResultReceiver(new Handler());

        //show newest posts first, for this load from from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();
        list_user_id = new ArrayList<>();

        panicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPanicBtnDialog();
            }
        });

        //handle fab click post
        createPostTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });

        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileFragment fragment1 = new ProfileFragment();
                getActivity().getSupportFragmentManager()
                        .beginTransaction().replace(R.id.content, fragment1)
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadPosts2();

        setHasOptionsMenu(true); //to show menu option in fragment
        // Inflate the layout for this fragment
        return view;
    }

    private void loadPosts2(){

        DatabaseReference mFriendsDatabase = FirebaseDatabase.getInstance().getReference("Friends");

        mFriendsDatabase.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list_user_id.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
//                            list_user_id = ds.getKey();

                            list_user_id.add(ds.getKey());

                            //path of all posts
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                            //get all data from this ref
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    postList.clear();
                                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                                        ModelPost modelPost = ds.getValue(ModelPost.class);

                                        for (int i=0; i<list_user_id.size() ;i++){
                                            if(modelPost.getUid().equals(uid) || modelPost.getUid().equals(list_user_id.get(i))){
                                                postList.add(modelPost);
                                            }
                                        }

                                        //adapter
                                        adapterPosts = new AdapterPosts(getActivity(), postList);

                                        //set adapter to recyclerView
                                        recyclerView.setAdapter(adapterPosts);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //in case of error
//                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void searchPosts(String searchQuery){
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);

                        //adapter
                        adapterPosts = new AdapterPosts(getActivity(), postList);

                        //set adapter to recyclerView
                        recyclerView.setAdapter(adapterPosts);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in ca of error
                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    uid = ds.getKey();
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    if (mActivity == null) {
                        return;
                    }

                    if (!myDp.equals("")) {
                        try {
                            //if image is received then set
//                            Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                            Glide.with(mActivity)
                                    .load(myDp)
                                    .placeholder(R.drawable.ic_default_img)
                                    .into(avatarIv);
                        } catch (Exception e) {
//                            Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                            Glide.with(mActivity)
                                    .load(R.drawable.ic_default_img)
                                    .into(avatarIv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showPanicBtnDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Request Aid");

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        TextView textView = new TextView(getActivity());
        textView.setText(R.string.confirm_aid);
        textView.setTextSize(14f);
        textView.setTextColor(Color.parseColor("#FF000000"));
        textView.setMaxLines(3);
        textView.setPadding(30,20,30,20);
        linearLayout.addView(textView);

        builder.setView(linearLayout);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //check if permission enabled or not

                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    //NOT GRANTED
                    //REQ Permission
                    requestPermissions(locationPermission,REQUEST_CODE_LOCATION_PERMISSION);

                }else getAddress();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //create and show dialog
        builder.create().show();
    }

    @SuppressLint("MissingPermission")
    private void getAddress() {

        pd.setMessage("Sharing your location address to your friends...");
        pd.show();

        DatabaseReference mFriendRef = FirebaseDatabase.getInstance().getReference("Friends");
        mFriendRef.orderByKey().equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                        //get coordinate of curr location
                        LocationRequest locationRequest = new LocationRequest();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(3000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        LocationServices.getFusedLocationProviderClient(getActivity())
                                .requestLocationUpdates(locationRequest, new LocationCallback() {
                                    @SuppressLint("MissingPermission")
                                    @Override
                                    public void onLocationResult(@NonNull LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        LocationServices.getFusedLocationProviderClient(getContext())
                                                .removeLocationUpdates(this);
                                        if (locationResult.getLocations().size() > 0){
                                            int latestLocationIndex = locationResult.getLocations().size() -1;

                                            //get curr latitude & longitude
                                            double latitude =
                                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();

                                            double longitude =
                                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();

                                            Location location = new Location("providerNA");
                                            location.setLatitude(latitude);
                                            location.setLongitude(longitude);
                                            fetchAddressFromLatLong(location);
                                        }
                                        else {
                                            pd.dismiss();
                                        }
                                    }
                                }, Looper.getMainLooper());
                    }
                    else{
                        pd.dismiss();
                        Toast.makeText(
                                getContext(),
                                "You don't have friends to share your location",
                                Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void fetchAddressFromLatLong(Location location){
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        getContext().startService(intent);
    }

    void uploadAddress(String address){
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("wId", timestamp);
        hashMap.put("sUid", uid);
        hashMap.put("uName", myName);
        hashMap.put("uDp", myDp);
        hashMap.put("timestamp", timestamp);
        hashMap.put("wAddress", address);

        DatabaseReference mRequestAidDb = FirebaseDatabase.getInstance().getReference("Request_aid");
        mRequestAidDb.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addtoTheirNotif(timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        "Failed to upload address. please check your internet connection",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AddressResultReceiver extends ResultReceiver{

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS_RESULT){
                String address = resultData.getString(Constants.RESULT_DATA_KEY);

                uploadAddress(address);
                Toast.makeText(getContext(), "Sending Request Success", Toast.LENGTH_SHORT).show();

            }
            else {
                Toast.makeText(getContext(), "Failed getting result address", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }

    private void addtoTheirNotif(String timestamp){

        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference mFriendsDatabase = FirebaseDatabase.getInstance().getReference("Friends");

        mFriendsDatabase.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String friend_user_id = ds.getKey();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("wId", timestamp);
                            hashMap.put("pUid", friend_user_id);
                            hashMap.put("sUid", firebaseAuth.getCurrentUser().getUid());
                            hashMap.put("notification", "Requesting health aid");
                            hashMap.put("timestamp", timestamp);

                            mUserDatabase.child(friend_user_id).child("Notifications").child(timestamp)
                                    .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendPushNotif(friend_user_id, "New Urgent Request", myName, timestamp);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendPushNotif(String list_user_id, String title, final String name, String timestamp){
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(list_user_id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            ""+uid,
                            name+" requesting health aid",
                            ""+title,
                            ""+list_user_id,
                            ""+timestamp,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getAddress();
            }
            else {
                Toast.makeText(getContext(),
                            "Plase allow app location access for sharing location to your friends"
                            , Toast.LENGTH_SHORT).show();
                }
                return;
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
        menu.findItem(R.id.action_chatlist).setVisible(false);
        menu.findItem(R.id.action_nearby_clinic).setVisible(true);

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
                    loadPosts2();
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
                    loadPosts2();
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
        actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();
        int id = item.getItemId();
        if (id == R.id.action_nearby_clinic){

            startActivity(new Intent(getActivity(), MapsActivity.class));
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