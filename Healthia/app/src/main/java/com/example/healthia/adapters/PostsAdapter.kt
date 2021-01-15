package com.example.healthia.adapters

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.AddPostActivity
import com.example.healthia.R
import com.example.healthia.models.ModelPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class PostsAdapter(var context: Context, var postList: MutableList<ModelPost?>) : RecyclerView.Adapter<PostsAdapter.MyHolder>() {
    var myUid: String
    private val likesRef //for likes database node
            : DatabaseReference
    private val postsRef //reference of posts
            : DatabaseReference
    var mProcessLike = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        //inflate layout row post.xml
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_posts, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data
        val uid = postList[position]!!.uid
        val uEmail = postList[position]!!.uEmail
        val uName = postList[position]!!.uName
        val uDp = postList[position]!!.uDp
        val pId = postList[position]!!.pId
        val pTitle = postList[position]!!.pTitle
        val pDescription = postList[position]!!.pDescription
        val pImage = postList[position]!!.pImage
        val pTimeStamp = postList[position]!!.pTime
        val pLikes = postList[position]!!.pLikes //contains total number of likes for a post

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = pTimeStamp!!.toLong()
        val pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString()

        //set data
        holder.uNameTv.text = uName
        holder.pTimeTv.text = pTime
        holder.pTitleTv.text = pTitle
        holder.pDescriptionTv.text = pDescription
        holder.pLikesTv.text = "$pLikes Likes" //e.g 100 Likes
        setLikes(holder, pId!!)

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
            //show imageView
            holder.pImageIv.visibility = View.VISIBLE
            try {
                Picasso.get().load(pImage).into(holder.pImageIv)
            } catch (e: Exception) {
            }
        }


        //handle button clicks,
        holder.moreBtn.setOnClickListener { showMoreOptions(holder.moreBtn, uid!!, myUid, pId!!, pImage!!) }
        holder.likeBtn.setOnClickListener {
            //get total number of likes for the post, whose like like button clicked
            //if currently signed in user has not liked it before
            //increase value by 1, otherwise decrease value by 1
            val pLikes = postList[position]?.pLikes?.toInt()
            mProcessLike = true
            //get id of the post clicked
            val postIde = postList[position]?.pId
            likesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (mProcessLike) {
                        mProcessLike = if (dataSnapshot.child(postIde!!).hasChild(myUid)) {
                            //already liked, so remove like
                            postsRef.child(postIde).child("pLikes").setValue("" + (pLikes!! - 1))
                            likesRef.child(postIde).child(myUid).removeValue()
                            false
                        } else {
                            //not liked, like it
                            postsRef.child(postIde).child("pLikes").setValue("" + (pLikes!! + 1))
                            likesRef.child(postIde).child(myUid).setValue("Liked")
                            false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
        holder.commentBtn.setOnClickListener { //will implement later
            Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setLikes(holder: MyHolder, postKey: String) {
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this (SignedIn) user
                    * Change drawable left icon of like button
                    * Change text of like button from "Like" to "Liked"*/
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0)
                    holder.likeBtn.text = "Liked"
                } else {
                    //user has not liked this post
                    /*To indicate that the post is not liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "Liked" to "Like"*/
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0)
                    holder.likeBtn.text = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showMoreOptions(moreBtn: ImageButton, uid: String, myUid: String, pId: String, pImage: String) {

        //creating popup menu currently having option Delete, we will add more option later
        val popupMenu = PopupMenu(context, moreBtn, Gravity.END)

        //show delete option  in only post(s) of currently signed-in user
        if (uid == myUid) {
            //add items in menu
            popupMenu.menu.add(Menu.NONE, 0, 0, "Delete")
            popupMenu.menu.add(Menu.NONE, 1, 0, "Edit")
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == 0) {
                //delete is clicked
                beginDelete(pId, pImage)
            } else if (id == 1) {
                //Edit is clicked
                //start AddPostActivity with key "editPost" and the id of the post clicked
                val intent = Intent(context, AddPostActivity::class.java)
                intent.putExtra("key", "editPost")
                intent.putExtra("editPostId", pId)
                context.startActivity(intent)
            }
            false
        }
        //show menu
        popupMenu.show()
    }

    private fun beginDelete(pId: String, pImage: String) {
        //post can be with or without image
        if (pImage == "noImage") {

            //post is without image
            deleteWithoutImage(pId)
        } else {

            //post is with image
            deleteWithImage(pId, pImage)
        }
    }

    private fun deleteWithImage(pId: String, pImage: String) {
        //progress bar
        val pd = ProgressDialog(context)
        pd.setMessage("Deleting...")

        /*Steps:
        * 1) Delete Image using url
        * 2) Delete from database using post id*/
        val picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage)
        picRef.delete()
            .addOnSuccessListener {
                //image deleted, now delete database
                val fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId)
                fquery.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (ds in dataSnapshot.children) {
                            ds.ref.removeValue()
                        }
                        //deleted
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                        pd.dismiss()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            .addOnFailureListener { e -> //failed, can't go further
                pd.dismiss()
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteWithoutImage(pId: String) {
        val pd = ProgressDialog(context)
        pd.setMessage("Deleting...")
        val fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId)
        fquery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    ds.ref.removeValue()
                }
                //deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                pd.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return postList.size
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

    init {
        myUid = FirebaseAuth.getInstance().currentUser!!.uid
        likesRef = FirebaseDatabase.getInstance().reference.child("Likes")
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
    }
}