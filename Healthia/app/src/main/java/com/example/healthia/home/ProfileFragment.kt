package com.example.healthia.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.healthia.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    //firebase
    var firebaseAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null

    //storage
    var storageReference: StorageReference? = null

    //path where images of user profile and cover will be stored
    var storagePath = "Users_Profile_Cover_Imgs/"

    //views from xml
    lateinit var avatarTv: ImageView
    lateinit var coverIv: ImageView
    lateinit var nameTv: TextView
    lateinit var emailTv: TextView
    lateinit var phoneTv: TextView
    lateinit var fab: FloatingActionButton

    //progress dialog
    var pd: ProgressDialog? = null

    //arrays of permission to be required
    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>

    //uri of picked image
    var image_uri: Uri? = null

    //for checking profile or cover photo
    var profileOrCoverPhoto: String? = null

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        //to show menu option in fragment
        setHasOptionsMenu(true)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference //firebase storage reference

        //init arrays of permissions
        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init views
        avatarTv = view.findViewById(R.id.avatarIv)
        coverIv = view.findViewById(R.id.coverIv)
        nameTv = view.findViewById(R.id.nameTv)
        emailTv = view.findViewById(R.id.emailTv)
        phoneTv = view.findViewById(R.id.phoneTv)
        fab = view.findViewById(R.id.fab)
        pd = ProgressDialog(activity)
        /*We have to get info of currently signed in user. we can get it using user's email
         * or uid
         * I'm gonna retrieve user detail using email
         * By using orderByChild query we will show the detail from a node
         * whose key named email has value equal to currently signed in email
         * it will search all nodes, where the key matches it will get its detail*/
        val query = databaseReference!!.orderByChild("email").equalTo(user!!.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //check until required data get
                for (ds in dataSnapshot.children) {

                    //get    data
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    val phone = "" + ds.child("phone").value
                    val image = "" + ds.child("image").value
                    val cover = "" + ds.child("cover").value

                    //set data
                    nameTv.setText(name)
                    emailTv.setText(email)
                    phoneTv.setText(phone)
                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarTv)
                    } catch (e: Exception) {
                        //if there is any exception while getting image the set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarTv)
                    }
                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv)
                    } catch (e: Exception) {
                        //if there is any exception while getting image the set default
                    }

                    progressBar.visibility = View.GONE
                    profileScrollView.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //fab button click
        fab.setOnClickListener(View.OnClickListener { showEditProfileDialog() })
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun checkStoragePermission(): Boolean {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        val result = (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        val result1 = (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        return result && result1
    }

    private fun requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE)
    }

    private fun showEditProfileDialog() {
        /*Show dialog containing options
         * 1. Edit Profile picture
         * 2. Edit cover photo
         * 3. Edit Name
         * 4. Edit Phone*/

        //options to show in dialog
        val options = arrayOf("Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone")
        //alert dialog
        val builder = AlertDialog.Builder(activity)
        //set title
        builder.setTitle("Choose Action")
        //set items to dialog
        builder.setItems(options) { dialogInterface, which ->
            //handle dialog item clicks
            if (which == 0) {
                //Edit profile clicked
                pd!!.setMessage("Update Profile Picture")
                profileOrCoverPhoto = "image" //i.e. changing profile picture, make sure to assign same value
                showImagePicDialog()
            } else if (which == 1) {
                //Edit cover clicked
                pd!!.setMessage("Update Cover Photo")
                profileOrCoverPhoto = "cover" //i.e. changing cover photo, make sure to assign same value
                showImagePicDialog()
            } else if (which == 2) {
                //Edit Name clicked
                pd!!.setMessage("Update Name")
                //calling method pass key "name" as parameter to update it's value in database
                showNamePhoneUpdateDialog("name")
            } else if (which == 3) {
                //Edit Phone clicked
                pd!!.setMessage("Update Phone")
                showNamePhoneUpdateDialog("phone")
            }
        }
        //create and show dialog
        builder.create().show()
        //after reaching this line check firebase storage rules
    }

    private fun showNamePhoneUpdateDialog(key: String) {
        /*parameter "key" will contain value:
         *   either "name" which is key in user's database which is used to update user's name
         *   or      "phone" which is key in user's database which is used to update user's phone*/

        //custom dialog
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Update $key") // e.g. Update name OR Update Phone
        //set layout of dialog
        val linearLayout = LinearLayout(activity)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(10, 10, 10, 10)

        //add edit text
        val editText = EditText(activity)
        editText.hint = "Enter $key" //hint e.g. Edit name or Edit Phone
        linearLayout.addView(editText)
        builder.setView(linearLayout)

        //add buttons in dialog to update
        builder.setPositiveButton("Update") { dialogInterface, i ->
            //input text from edit text
            val value = editText.text.toString().trim { it <= ' ' }
            //validate if user has entered something or not
            if (!TextUtils.isEmpty(value)) {
                pd!!.show()
                val result = HashMap<String, Any>()
                result[key] = value
                databaseReference!!.child(user!!.uid).updateChildren(result)
                        .addOnSuccessListener { //update dismiss progress
                            pd!!.dismiss()
                            Toast.makeText(activity, "Updated...", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e -> //failed dismiss progress, get and show error message
                            pd!!.dismiss()
                            Toast.makeText(activity, "" + e.message, Toast.LENGTH_SHORT).show()
                        }
            } else {
                Toast.makeText(activity, "Please enter $key", Toast.LENGTH_SHORT).show()
            }
        }
        //add buttons in dialog to cancel
        builder.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
        //create and show dialog
        builder.create().show()
    }

    private fun showImagePicDialog() {
        //options to show in dialog
        val options = arrayOf("Camera", "Gallery")
        //alert dialog
        val builder = AlertDialog.Builder(activity)
        //set title
        builder.setTitle("Pick Image From")
        //set items to dialog
        builder.setItems(options) { dialogInterface, which ->
            //handle dialog item clicks
            if (which == 0) {
                //Camera clicked
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickFromCamera()
                }
            } else if (which == 1) {
                //Gallery clicked
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        }
        //create and show dialog
        builder.create().show()

        /*for picking image from:
         * 1. camera [camera and storage permission required]
         * 2. Gallery [storage permission required]*/
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        /*This method called when user press Allow or Deny from permission request dialog
         * here we will handle permission cases(allowed & denied)*/
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                //picking from camera, first check if camera and storage permission allowed or not
                if (grantResults.size > 0) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera()
                    } else {
                        //permission denied
                        Toast.makeText(activity, "Please enable camera & storage permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {


                //picking from gallery, first check if storage permission allowed or not
                if (grantResults.size > 0) {
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery()
                    } else {
                        //permission denied
                        Toast.makeText(activity, "Please enable storage permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /*This method will be called after picking image from Camera or Gallery*/
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE) {
                //image is picked from gallery, get url of image
                image_uri = data!!.data
                uploadProfileCoverPhoto(image_uri)
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadProfileCoverPhoto(uri: Uri?) {
        pd!!.show()
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
        val filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user!!.uid
        val storageReference2nd = storageReference!!.child(filePathAndName)
        storageReference2nd.putFile(uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    //image is uploaded to storage, now get it's url and store in user's database
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri = uriTask.result

                    //check if image is uploaded or not and url is received
                    if (uriTask.isSuccessful) {
                        //image uploaded
                        //add/update url in user's database
                        val results = HashMap<String?, Any>()
                        /*First Parameter is profileOrCoverPhoto that has value "image" or "cover"
                             * which are keys in user's database where uri of image will be saved in one of them
                             * second paramete rcontains the url of the image stored in firebase storage, this
                             * url will be saved as value against key "image" or "cover" */results[profileOrCoverPhoto] = downloadUri.toString()
                        databaseReference!!.child(user!!.uid).updateChildren(results)
                                .addOnSuccessListener { //url in database of user is added successfully
                                    //dismiss progress bar
                                    pd!!.dismiss()
                                    Toast.makeText(activity, "Image Updated...", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    pd!!.dismiss()
                                    Toast.makeText(activity, "Error Updating Image...", Toast.LENGTH_SHORT).show()
                                }
                    } else {
                        //error
                        pd!!.dismiss()
                        Toast.makeText(activity, "Some error occured", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e -> //there were some error(s), get and show error message, dismiss progress dialog
                    pd!!.dismiss()
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
    }

    private fun pickFromGallery() {
        //pick from gallery
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE)
    }

    private fun pickFromCamera() {
        //Intent of pick image from device camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        //put image uri
        image_uri = activity
                ?.getContentResolver()
                ?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //intent to start camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE)
    }

    companion object {
        //permission constants
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 200
        private const val IMAGE_PICK_GALLERY_REQUEST_CODE = 300
        private const val IMAGE_PICK_CAMERA_REQUEST_CODE = 400

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
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}