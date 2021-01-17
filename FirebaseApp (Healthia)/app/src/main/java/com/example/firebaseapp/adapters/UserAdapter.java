package com.example.firebaseapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.ChatActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    Context context;
    List<ModelUser> userList;

    //constructor
    public UserAdapter(Context context, List<ModelUser> userList) {
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

        //set daata
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);

        try {
            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(holder.mAvatarTv);

        }
        catch (Exception e){

        }

        //handle item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context,ChatActivity.class);
            intent.putExtra("hisUid", hisUID);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mAvatarTv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarTv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
