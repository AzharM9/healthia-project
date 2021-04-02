package com.example.firebaseapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterReply extends RecyclerView.Adapter<AdapterReply.MyHolder> {

    Context context;
    List<ModelReply> replyList;
    String myUid, postId;

    public AdapterReply(Context context, List<ModelReply> replyList, String myUid, String postId) {
        this.context = context;
        this.replyList = replyList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_reply, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        String uid = replyList.get(position).getUid();
        String name = replyList.get(position).getuName();
        String email = replyList.get(position).getuEmail();
        String image = replyList.get(position).getuDp();
        String cid = replyList.get(position).getrId();
        String comment = replyList.get(position).getReply();
        String timestamp = replyList.get(position).getTimestamp();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));

        String pTime = DateFormat.format("dd/MMM/yyyy hh:mm aa", calendar).toString();

        //set the data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        //set user dp
        if(!image.equals("")) {
            try {
                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
            } catch (Exception e) {

            }
        }

        //comment click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if comment is by currently signed in user or not
                if (myUid.equals(uid)) {
                    //my comment
                    //show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    //show dialog
                    builder.create().show();
                } else {
                    // no my comment
                    Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteComment(String cId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cId).removeValue(); //it will delete the comment

        //new update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        //declare views from row_comments.xml
        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
