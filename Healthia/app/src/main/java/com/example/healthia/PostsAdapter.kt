package com.example.firebaseapp

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.R
import com.squareup.picasso.Picasso
import java.util.*

class PostsAdapter(var context: Context?, var postList: List<ModelPost?>?) : RecyclerView.Adapter<PostsAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        //inflate layout row post.xml
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_posts, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data
        val uid = postList!![position]?.uid
        val uEmail = postList!![position]!!.uEmail
        val uName = postList!![position]!!.uName
        val uDp = postList!![position]!!.uDp
        val pId = postList!![position]!!.pId
        val pTitle = postList!![position]!!.pTitle
        val pDescription = postList!![position]!!.pDescription
        val pImage = postList!![position]!!.pImage
        val pTimeStamp = postList!![position]!!.pTime

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = pTimeStamp!!.toLong()
        val pTime = DateFormat.format("dd/MM/YYYY hh:mm aa", calendar).toString()

        //set data
        holder.uNameTv.text = uName
        holder.pTimeTv.text = pTime
        holder.pTitleTv.text = pTitle
        holder.pDescriptionTv.text = pDescription

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv)
        } catch (e: Exception) {
        }

        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView
        if (pImage == "noImage") {
            //hide imageView
            holder.pImageIv.visibility = View.GONE
        } else {
            try {
                Picasso.get().load(pImage).into(holder.pImageIv)
            } catch (e: Exception) {
            }
        }


        //handle button clicks,
        holder.moreBtn.setOnClickListener { //will implement later
            Toast.makeText(context, "More", Toast.LENGTH_SHORT).show()
        }
        holder.likeBtn.setOnClickListener { //will implement later
            Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show()
        }
        holder.commentBtn.setOnClickListener { //will implement later
            Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return postList!!.size
    }

    //view holder class
    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //views from item row post
        var uPictureIv: ImageView
        var pImageIv: ImageView
        var uNameTv: TextView
        var pTimeTv: TextView
        var pTitleTv: TextView
        var pDescriptionTv: TextView
        var pLikesTv: TextView
        var moreBtn: ImageButton
        var likeBtn: Button
        var commentBtn: Button

        init {

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv)
            pImageIv = itemView.findViewById(R.id.pImageIv)
            uNameTv = itemView.findViewById(R.id.uNameTv)
            pTimeTv = itemView.findViewById(R.id.pTimeTv)
            pTitleTv = itemView.findViewById(R.id.pTitleTv)
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv)
            pLikesTv = itemView.findViewById(R.id.pLikesTv)
            moreBtn = itemView.findViewById(R.id.moreBtn)
            likeBtn = itemView.findViewById(R.id.likeBtn)
            commentBtn = itemView.findViewById(R.id.commentBtn)
        }
    }
}