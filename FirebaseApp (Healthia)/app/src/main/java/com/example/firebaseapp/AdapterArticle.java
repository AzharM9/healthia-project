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

public class AdapterArticle extends RecyclerView.Adapter<AdapterArticle.MyHolder>{

    Context context;
    List<ModelArticle> articleList;

    String myUid;

    public AdapterArticle(Context context, List<ModelArticle> articleList) {
        this.context = context;
        this.articleList = articleList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout row post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_article, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = articleList.get(position).getUid();
        String uName = articleList.get(position).getuName();
        String uDp = articleList.get(position).getuDp();
        String aId = articleList.get(position).getaId();
        String aTitle = articleList.get(position).getaTitle();
        String aCategory = articleList.get(position).getaCategory();
        String aImage = articleList.get(position).getaImage();
        String aTimeStamp = articleList.get(position).getaTime();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(aTimeStamp));

        String fTime = DateFormat.format("MMM d, yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.aTimeTv.setText(fTime);
        holder.aTitleTv.setText(aTitle);
        holder.aCategoryTv.setText(aCategory);

        //set user dp
        try{
//            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
            Glide.with(context)
                    .load(uDp).placeholder(R.drawable.ic_default_img)
                    .apply(new RequestOptions().override(50,50))
                    .into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        //set post image
        //if there is no image i.e. aImage.equals("noImage") then hide ImageView
        if (aImage.equals("noImage")){
             //hide imageView
            holder.aImageIv.setVisibility(View.INVISIBLE);
        }
        else{
            //show imageView
            holder.aImageIv.setVisibility(View.VISIBLE);
            try{
//                Picasso.get().load(aImage).placeholder(R.drawable.ic_image_black_24).into(holder.aImageIv);
                Glide.with(context).load(aImage)
                        .placeholder(R.drawable.ic_image_black_24)
                        .apply(new RequestOptions().centerCrop())
                        .into(holder.aImageIv);
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

                showMoreOptions(holder.moreBtn, uid, myUid, aId, aImage);
            }
        });

        holder.aTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star PostDetailActivity
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("postId", aId);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //star PostDetailActivity
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("postId", aId);
                context.startActivity(intent);
            }
        });

        holder.aImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star PostDetailActivity
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("postId", aId);
                context.startActivity(intent);
            }
        });

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
                    builder.setMessage("Are you sure to delete this article?");
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
                    Intent intent = new Intent(context, AddArticleActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }
                else if(id == 2){
                    //star PostDetailActivity
                    Intent intent = new Intent(context, ArticleDetailActivity.class);
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
                        Query fquery = FirebaseDatabase.getInstance().getReference("Articles").orderByChild("aId").equalTo(pId);
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

        Query fquery = FirebaseDatabase.getInstance().getReference("Articles").orderByChild("aId").equalTo(pId);
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
        return articleList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from item row post
        ImageView uPictureIv, aImageIv;
        TextView uNameTv, aTimeTv, aTitleTv, aCategoryTv;
        ImageButton moreBtn;
        LinearLayout profileLayout, item;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            aImageIv = itemView.findViewById(R.id.aImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            aTimeTv = itemView.findViewById(R.id.aTimeTv);
            aCategoryTv = itemView.findViewById(R.id.aCategoryTag);
            aTitleTv = itemView.findViewById(R.id.aTitleTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            item = itemView.findViewById(R.id.item);

        }


    }
}
