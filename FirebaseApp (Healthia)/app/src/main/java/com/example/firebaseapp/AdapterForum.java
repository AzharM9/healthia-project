package com.example.firebaseapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebaseapp.activitys.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterForum extends RecyclerView.Adapter<AdapterForum.MyHolder>{

    Context context;
    List<ModelForum> forumList;

    String myUid;

    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postsRef; //reference of posts

    boolean mProcessLike = false;

    public AdapterForum(Context context, List<ModelForum> forumList) {
        this.context = context;
        this.forumList = forumList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
//        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout row post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_forum, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = forumList.get(position).getUid();
//        String uEmail = postList.get(position).getuEmail();
        String uName = forumList.get(position).getuName();
        String uDp = forumList.get(position).getuDp();
        String fId = forumList.get(position).getfId();
        String fTitle = forumList.get(position).getfTitle();
//        String pDescription = postList.get(position).getpDescription();
        String fImage = forumList.get(position).getfImage();
        String fTimeStamp = forumList.get(position).getfTime();
//        String pLikes = postList.get(position).getpLikes(); //contains total number of likes for a post
//        String pComments = postList.get(position).getpComments();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(fTimeStamp));

        String fTime = DateFormat.format("MMM d, yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.fTimeTv.setText(fTime);
        holder.fTitleTv.setText(fTitle);
//        holder.pDescriptionTv.setText(pDescription);
//        holder.pLikesTv.setText(pLikes +" Likes"); //e.g 100 Likes
//        holder.pCommentsTv.setText(pComments +" Comments"); //e.g 100 Likes
        //set likes for each post
//        setLikes(holder, fId);

        //set user dp
        try{
//            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
            Glide.with(context)
                    .load(uDp)
                    .placeholder(R.drawable.ic_default_img)
                    .apply(new RequestOptions().override(50,50))
                    .into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        //set post image
        //if there is no image i.e. fImage.equals("noImage") then hide ImageView
        if (fImage.equals("noImage")){
             //hide imageView
            holder.fImageIv.setVisibility(View.INVISIBLE);
        }
        else{
            //show imageView
            holder.fImageIv.setVisibility(View.VISIBLE);
            try{
//                Picasso.get().load(fImage).placeholder(R.drawable.ic_image_black_24).into(holder.fImageIv);
                Glide.with(context)
                        .load(fImage)
                        .placeholder(R.drawable.ic_image_black_24)
                        .apply(new RequestOptions().centerCrop())
                        .into(holder.fImageIv);
            }
            catch (Exception e){

            }
        }

        if (uid.equals(myUid)) {
            holder.moreBtn.setVisibility(View.VISIBLE);
        }


        //handle button clicks,
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showMoreOptions(holder.moreBtn, uid, myUid, fId, fImage);
            }
        });

        holder.fTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star PostDetailActivity
                Intent intent = new Intent(context, ForumDetailActivity.class);
                intent.putExtra("postId", fId);
                context.startActivity(intent);
            }
        });

        holder.fImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star PostDetailActivity
                Intent intent = new Intent(context, ForumDetailActivity.class);
                intent.putExtra("postId", fId);
                context.startActivity(intent);
            }
        });

//        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //get total number of likes for the post, whose like like button clicked
//                //if currently signed in user has not liked it before
//                //increase value by 1, otherwise decrease value by 1
//                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
//                mProcessLike = true;
//                //get id of the post clicked
//                final String postIde = postList.get(position).getpId();
//                likesRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (mProcessLike){
//                            if (dataSnapshot.child(postIde).hasChild(myUid)){
//                                //already liked, so remove like
//                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
//                                likesRef.child(postIde).child(myUid).removeValue();
//                                mProcessLike = false;
//                            }
//                            else{
//                                //not liked, like it
//                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
//                                likesRef.child(postIde).child(myUid).setValue("Liked");
//                                mProcessLike = false;
//
//                                addToHisNotifications(""+uid, ""+fId, "Liked your post");
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//        });
//
//        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //star PostDetailActivity
//                Intent intent = new Intent(context, ForumDetailActivity.class);
//                intent.putExtra("postId", fId);
//                context.startActivity(intent);
//            }
//        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*click to go to ThereProfileActivity with uid, this uid is of clicked user
                * which will be used to show user specific data/post*/
                if (!uid.equals(myUid)) {
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                }
            }
        });


    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String > hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

//    private void setLikes(MyHolder holder, String postKey) {
//        likesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child(postKey).hasChild(myUid)){
//                    //user has liked this post
//                    /*To indicate that the post is liked by this (SignedIn) user
//                    * Change drawable left icon of like button
//                    * Change text of like button from "Like" to "Liked"*/
//                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
//                    holder.likeBtn.setText("Liked");
//                }
//                else{
//                    //user has not liked this post
//                    /*To indicate that the post is not liked by this (SignedIn) user
//                     * Change drawable left icon of like button
//                     * Change text of like button from "Liked" to "Like"*/
//                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
//                    holder.likeBtn.setText("Like");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {

        //creating popup menu currently having option Delete, we will add more option later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option  in only post(s) of currently signed-in user
        if(uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    //delete is clicked
                    //show delete message confirm dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this post?");
                    //delete button
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beginDelete(pId, pImage);
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
                    Intent intent = new Intent(context, AddForumActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }
                else if(id == 2){
                    //star PostDetailActivity
                    Intent intent = new Intent(context, ForumDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        
        if(pImage.equals("noImage")){
            
            //post is without image
            deleteWithoutImage(pId);
        }
        else{
            
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
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
                        Query fquery = FirebaseDatabase.getInstance().getReference("Forums").orderByChild("fId").equalTo(pId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                //deleted
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Forums").orderByChild("fId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                //deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from item row post
        ImageView uPictureIv, fImageIv;
        TextView uNameTv, fTimeTv, fTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
//            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            fImageIv = itemView.findViewById(R.id.fImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            fTimeTv = itemView.findViewById(R.id.fTimeTv);
            fTitleTv = itemView.findViewById(R.id.fTitleTv);
//            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
//            pLikesTv = itemView.findViewById(R.id.pLikesTv);
//            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
//            likeBtn = itemView.findViewById(R.id.likeBtn);
//            commentBtn = itemView.findViewById(R.id.commentBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }


    }
}
