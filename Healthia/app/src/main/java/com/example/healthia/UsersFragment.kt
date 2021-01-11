package com.example.healthia

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [UsersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var userAdapter: UserAdapter
    lateinit var userList: MutableList<ModelUser?>

    //firebase auth
    var firebaseAuth: FirebaseAuth? = null

    // TODO: Rename and change types of parameters
    private val mParam1: String? = null
    private val mParam2: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        //to show menu option in fragment
        setHasOptionsMenu(true)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //init recyclerView
        recyclerView = view.findViewById(R.id.users_recyclerView)

        //set it's properties
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(LinearLayoutManager(activity))

        //init user list
        userList = ArrayList()

        //getAll users
        allUsers
        return view
    }

    private fun searchUsers(query: String) {
        //get current user
        val fUser = FirebaseAuth.getInstance().currentUser

        //get path of database named "Users" containing users info
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        //get all data from path
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList!!.clear()
                for (ds in dataSnapshot.children) {
                    val modelUser = ds.getValue(ModelUser::class.java)

                    /*Condition to fulfill search:
                    *  1) User not current user
                    *  2) The user name or email contains text entered in SearchView
                    * (case insensitive)*/
                    //get all searched users except currently signed user
                    if (modelUser?.uid != fUser!!.uid) {
                        if (modelUser?.name?.toLowerCase()!!.contains(query.toLowerCase()) ||
                                modelUser?.email?.toLowerCase()!!.contains(query.toLowerCase())) {
                            userList!!.add(modelUser)
                        }
                    }

                    //adapter
                    userAdapter = UserAdapter(activity, userList)

                    //refresh adapter
                    userAdapter!!.notifyDataSetChanged()

                    //set adapter to recycler view
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }//get all users except currently signed user

    //adapter

    //set adapter to recycler view
    //get current user
    private val allUsers:

    //get path of database named "Users" containing users info

    //get all data from path
            Unit
        private get() {
            //get current user
            val fUser = FirebaseAuth.getInstance().currentUser

            //get path of database named "Users" containing users info
            val ref = FirebaseDatabase.getInstance().getReference("Users")

            //get all data from path
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList!!.clear()
                    for (ds in dataSnapshot.children) {
                        val modelUser = ds.getValue(ModelUser::class.java)

                        //get all users except currently signed user
                        if (modelUser?.uid != fUser!!.uid) {
                            userList!!.add(modelUser)
                        }

                        //adapter
                        userAdapter = UserAdapter(activity, userList)

                        //set adapter to recycler view
                        recyclerView!!.adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
//            mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(activity, MainActivity::class.java))
            activity!!.finish()
        }
    }

    //inflate option menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflating menu
        inflater.inflate(R.menu.option_menu, menu)

        //SearchView
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView

        //search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim { it <= ' ' })) {
                    //search text contains text, search
                    searchUsers(query)
                } else {
                    //search text empety, get all users
                    allUsers
                }
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                //called whenever user press any single letter
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim { it <= ' ' })) {
                    //search text contains text, search
                    searchUsers(query)
                } else {
                    //search text empety, get all users
                    allUsers
                }
                return true
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
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UsersFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): UsersFragment {
            val fragment = UsersFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}