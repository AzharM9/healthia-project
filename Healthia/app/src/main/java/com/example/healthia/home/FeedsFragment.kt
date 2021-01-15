package com.example.healthia.home

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthia.AddPostActivity
import com.example.healthia.LoginOrRegisterActivity
import com.example.healthia.R
import com.example.healthia.adapters.PostsAdapter
import com.example.healthia.models.ModelPost
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedsFragment : Fragment() {

    //firebase auth
    var firebaseAuth: FirebaseAuth? = null
    lateinit var recyclerView: RecyclerView
    lateinit var postList: MutableList<ModelPost?>
    lateinit var postsAdapter: PostsAdapter

    lateinit var fab: FloatingActionButton

    //user info
    var email: String? = null
    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_feeds, container, false)

        setHasOptionsMenu(true) //to show menu option in fragment

        //init fab
        fab = view.findViewById(R.id.fab)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerview)
        val layoutManager = LinearLayoutManager(activity)

        //show newest posts first, for this load from from last
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true

        //set layout to recycler view
        recyclerView.setLayoutManager(layoutManager)

        //init post list
        postList = ArrayList()
        loadPosts()

        fab.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                startActivity(Intent(activity, AddPostActivity::class.java))
            }
        })

        return view
    }

    private fun loadPosts() {
        //path of all posts
        val ref = FirebaseDatabase.getInstance().getReference("Posts")

        //get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList!!.clear()
                for (ds in dataSnapshot.children) {
                    val modelPost = ds.getValue(ModelPost::class.java)
                    postList!!.add(modelPost)

                    //adapter
                    postsAdapter = PostsAdapter(activity!!, postList)

                    //set adapter to recyclerView
                    recyclerView!!.adapter = postsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
//                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
    }

    private fun searchPosts(searchQuery: String) {
        //path of all posts
        val ref = FirebaseDatabase.getInstance().getReference("Posts")

        //get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList!!.clear()
                for (ds in dataSnapshot.children) {
                    val modelPost = ds.getValue(ModelPost::class.java)
                    if (modelPost!!.pTitle!!.toLowerCase().contains(searchQuery.toLowerCase()) ||
                        modelPost.pDescription!!.toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList!!.add(modelPost)

                        //adapter
                        postsAdapter = PostsAdapter(activity!!, postList)

                        //set adapter to recyclerView
                        recyclerView!!.adapter = postsAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //in ca of error
                Toast.makeText(activity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            email = user.email
            uid = user.uid
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(activity, LoginOrRegisterActivity::class.java))
            activity!!.finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflating menu
        inflater.inflate(R.menu.option_menu, menu)

        //searchView to search posts by posts title/description
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView

        //search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s)
                } else {
                    loadPosts()
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s)
                } else {
                    loadPosts()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    //handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}