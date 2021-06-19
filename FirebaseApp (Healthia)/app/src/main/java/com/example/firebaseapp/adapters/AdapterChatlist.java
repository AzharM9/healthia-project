package com.example.firebaseapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebaseapp.activitys.ChatActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUser> userList;
    private HashMap<String, String> lastMessageMap;
    private HashMap<String, Boolean> seenMessageMap;
    private HashMap<String, String> senderMap;

    //constructor
    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        seenMessageMap = new HashMap<>();
        senderMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_chat_list.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);
        Boolean seenMessage = seenMessageMap.get(hisUid);
        String sender = senderMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if(lastMessage==null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try{
//            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
            Glide.with(context).load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        }catch (Exception e){
//            Picasso.get().load(R.drawable.ic_default_img).into(holder.profileIv);
            Glide.with(context).load(R.drawable.ic_default_img).into(holder.profileIv);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(seenMessage != null && seenMessage.equals(false) && sender != null && !sender.equals(user.getUid())){
            holder.seenMessageIv.setVisibility(View.VISIBLE);
        } else{
            holder.seenMessageIv.setVisibility(View.GONE);
        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });


    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }
    public void setSeenMessageMap(String userId, Boolean isSeen, String senderId) {
        seenMessageMap.put(userId, isSeen);
        senderMap.put(userId, senderId);
    }

    @Override
    public int getItemCount() {
        //size of the list
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        // views of row_chatlist.xml
        ImageView profileIv, seenMessageIv;
        TextView nameTv, lastMessageTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            seenMessageIv = itemView.findViewById(R.id.seenMessageIv);

        }

    }
}
