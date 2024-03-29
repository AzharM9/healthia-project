package com.example.firebaseapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseapp.AboutUsFragment;
import com.example.firebaseapp.activitys.DashboardActivity;
import com.example.firebaseapp.activitys.AddNewFriendsActivity;
import com.example.firebaseapp.activitys.MainActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private Activity mActivity;

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views from xml
    CircleImageView avatarIv;
    CardView doctorTagCv;
    ImageView coverIv;
    TextView nameTv, emailTv, phoneTv, ageTv, weightTv, heightTv;
    Button mFriendList;
    MaterialButton fab;
    SwitchMaterial switchBtn;
    RecyclerView postRecyclerView;

    //progress dialog
    ProgressDialog pd;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    //arrays of permission to be required
    String cameraPermissions[];
    String storagePermissions[];

    ActionBar actionBar;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri of picked image
    Uri image_uri, downloadUri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ((DashboardActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference(); //firebase storage reference

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        doctorTagCv = view.findViewById(R.id.doctorTag);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        ageTv = view.findViewById(R.id.ageTv);
        weightTv = view.findViewById(R.id.weightTv);
        heightTv = view.findViewById(R.id.heightTv);
        switchBtn = view.findViewById(R.id.switchBtn);
        mFriendList = view.findViewById(R.id.friendList);
        fab = view.findViewById(R.id.fab);
        postRecyclerView = view.findViewById(R.id.recyclerview_posts);


        pd = new ProgressDialog(getActivity());
        /*We have to get info of currently signed in user. we can get it using user's email
         * or uid
         * I'm gonna retrieve user detail using email
         * By using orderByChild query we will show the detail from a node
         * whose key named email has value equal to currently signed in email
         * it will search all nodes, where the key matches it will get its detail*/

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    if (mActivity == null) {
                        return;
                    }

                    //get    data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String age = "" + ds.child("age").getValue();
                    String weight = "" + ds.child("weight").getValue();
                    String height = "" + ds.child("height").getValue();
                    String hideData = "" + ds.child("hideData").getValue();
                    String role = ""+ds.child("role").getValue();

                    if (role.equals("Doctor")) {
                        doctorTagCv.setVisibility(View.VISIBLE);
                    }else doctorTagCv.setVisibility(View.GONE);

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    ageTv.setText(age+" years");
                    weightTv.setText(weight+" kg");
                    heightTv.setText(height+" cm");

                    if (!image.equals("")){
                        try {
                            //if image is received then set
//                            Picasso.get().load(image).into(avatarTv);
                            Glide.with(mActivity)
                                    .load(image)
                                    .placeholder(R.drawable.ic_default_img_white)
                                    .apply(new RequestOptions().override(180,180))
                                    .into(avatarIv);
                        } catch (Exception e) {
                            //if there is any exception while getting image then set default
//                            Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                            Glide.with(mActivity)
                                    .load(R.drawable.ic_default_img_white)
                                    .apply(new RequestOptions().override(120,120))
                                    .into(avatarIv);
                        }
                    }
                    try {
                        //if cover is received then set
//                        Picasso.get().load(cover).into(coverIv);
                        Glide.with(mActivity)
                                .load(cover)
                                .apply(new RequestOptions().centerCrop())
                                .into(coverIv);

                    } catch (Exception e) {
                        //if there is any exception while getting image the set default
                    }

                    if (hideData.equals("true")){
                        switchBtn.setChecked(true);
                    } else {
                        switchBtn.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    //set data to private
                    showOrHideData(isChecked);
                } else {
                    //set data to public
                    showOrHideData(isChecked);
                }
            }
        });

        mFriendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(), AddNewFriendsActivity.class));
            }
        });

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();

        return view;
    }

    private void showOrHideData(boolean isChecked) {
        if (isChecked){
            firebaseDatabase.getReference("Users").child(user.getUid()).child("hideData")
                    .setValue("true");
        } else {
            firebaseDatabase.getReference("Users").child(user.getUid()).child("hideData")
                    .setValue("false");
        }
    }

    private void loadMyPosts() {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private void showEditProfileDialog() {
        /*Show dialog containing options
         * 1. Edit Profile picture
         * 2. Edit cover photo
         * 3. Edit Name
         * 4. Edit Phone
         * 5. Edit Age
         * 6. Edit Weight
         * 7. Edit Height*/

        //options to show in dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone",
                "Edit Age", "Edit Weight", "Edit Height"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //Edit profile clicked
                    pd.setMessage("Update Profile Picture");
                    profileOrCoverPhoto = "image"; //i.e. changing profile picture, make sure to assign same value
                    showImagePicDialog();
                } else if (which == 1) {
                    //Edit cover clicked
                    pd.setMessage("Update Cover Photo");
                    profileOrCoverPhoto = "cover"; //i.e. changing cover photo, make sure to assign same value
                    showImagePicDialog();
                } else if (which == 2) {
                    //Edit Name clicked
                    pd.setMessage("Update Name");
                    //calling method pass key "name" as parameter to update it's value in database
                    showDataUpdateDialog("name");

                } else if (which == 3) {
                    //Edit Phone clicked
                    pd.setMessage("Update Phone");
                    showDataUpdateDialog("phone");

                }else if (which == 4) {
                    //Edit Phone clicked
                    pd.setMessage("Update Age");
                    showDataUpdateDialog("age");

                }else if (which == 5) {
                    //Edit Phone clicked
                    pd.setMessage("Update Weight");
                    showDataUpdateDialog("weight");

                }else if (which == 6) {
                    //Edit Phone clicked
                    pd.setMessage("Update Height");
                    showDataUpdateDialog("height");

                }
            }
        });
        //create and show dialog
        builder.create().show();
        //after reaching this line check firebase storage rules
    }

    private void showDataUpdateDialog(String key) {
        /*parameter "key" will contain value:
         *   either "name" which is key in user's database which is used to update user's name
         *   or      "phone" which is key in user's database which is used to update user's phone*/

        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key); // e.g. Update name OR Update Phone
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        //add edit text
        EditText editText = new EditText(getActivity());
        if(key.equals("name")){
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if (key.equals("phone")){
            editText.setInputType(InputType.TYPE_CLASS_PHONE);
        }
        if (key.equals("age") || key.equals("weight") || key.equals("height")){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        editText.setHint("Enter " + key); //hint e.g. Edit name or Edit Phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if (key.equals("name") && value.length() < 3 || value.length() > 25){
                    Toast.makeText(getActivity(), "name at least 3-25 characters", Toast.LENGTH_SHORT).show();
                }else if (key.equals("phone") && value.length() < 10 || value.length() > 12){
                    Toast.makeText(getActivity(), "phone number at least 10-12 digits", Toast.LENGTH_SHORT).show();
                }else if (key.equals("age") && Integer.parseInt(value) > 100){
                    Toast.makeText(getActivity(), "age must below 100", Toast.LENGTH_SHORT).show();
                }else if (key.equals("weight") && Integer.parseInt(value) > 100){
                    Toast.makeText(getActivity(), "weight must below 100", Toast.LENGTH_SHORT).show();
                }else if (key.equals("height") && Integer.parseInt(value) > 200){
                    Toast.makeText(getActivity(), "height must below 200", Toast.LENGTH_SHORT).show();
                }else {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //update dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed dismiss progress, get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    //if user edit his name, also change it from his posts
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //update name in current users comments on post
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")) {
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(child1).child("Comments").orderByChild("uid").equalTo("uid");
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
        });
        //add buttons in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //options to show in dialog
        String options[] = {"Camera", "Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    //Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();

        /*for picking image from:
         * 1. camera [camera and storage permission required]
         * 2. Gallery [storage permission required]*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method called when user press Allow or Deny from permission request dialog
         * here we will handle permission cases(allowed & denied)*/

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera, first check if camera and storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {

                //picking from gallery, first check if storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*This method will be called after picking image from Camera or Gallery*/
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE) {
                //image is picked from gallery, get url of image
                image_uri = data.getData();
                CropImage.activity(image_uri)
                        .start(getContext(),this);
                uploadProfileCoverPhoto(image_uri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from camera, get uri of image
                CropImage.activity(image_uri)
                        .start(getContext(),this);
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                image_uri = result.getUri();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri)       {
        pd.show();
        /*Instead of creating separate function for Profile Picture and Cover Photo
         * I'm doing work for both in same function
         *
         * to add check I will add a string variable and assign it value "image" when user clicks
         * "Edit Profile Pic", and assign it value cover when user clicks "Edit Cover Photo"
         * Here: image is the key in each user containing url of user's profile picture
         *       cover is the key in each user containing url of iser's cover photo*/

        /*the parameter image_url contains the uri of image picked either from camera or gallery
         * We will use UID of the currently signed in user as name of the image so there will be only one image
         * profile and one image for cover for each user*/

        //path and name of image to store in firebase storage
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/update url in user's database

                            HashMap<String, Object> results = new HashMap<>();
                            /*First Parameter is profileOrCoverPhoto that has value "image" or "cover"
                             * which are keys in user's database where uri of image will be saved in one of them
                             * second paramete rcontains the url of the image stored in firebase storage, this
                             * url will be saved as value against key "image" or "cover" */
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
//                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            if (profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                // update user image in current users comments on posts
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Comments")) {
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                        .child(child1).child("Comments").orderByChild("uid").equalTo("uid");
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds: snapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                        } else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some error(s), get and show error message, dismiss progress dialog
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void pickFromCamera() {
        //Intent of pick image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getActivity()
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);

    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here
            //set email of logged in user
//            mProfileTv.setText(user.getEmail());
            uid = user.getUid();
        }else{
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of online status of current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_chatlist).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_nearby_clinic).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        switch (id){
            case (R.id.action_logout):
                String timestamp = String.valueOf(System.currentTimeMillis());

                //set offline with last seen timestamp
                checkOnlineStatus(timestamp);
                firebaseAuth.signOut();
                checkUserStatus();
                break;

            case (R.id.action_chatlist):
                actionBar = ((DashboardActivity) getActivity()).getSupportActionBar();
                actionBar.setTitle("Chats");
//                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                ChatListFragment fragment1 = new ChatListFragment();
                FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.content, fragment1, "");
                ft1.addToBackStack(null);
                ft1.commit();
                break;

            case (R.id.action_about_us):
                AboutUsFragment fragment2 = new AboutUsFragment();
                FragmentTransaction ft2 = getActivity().getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.content, fragment2, "");
                ft2.addToBackStack(null);
                ft2.commit();
                break;

            case (R.id.action_feedback):
                Uri uri = Uri.parse("https://bit.ly/app_healthia"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
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