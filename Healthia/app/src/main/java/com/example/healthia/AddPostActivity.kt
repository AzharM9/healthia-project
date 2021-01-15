package com.example.healthia

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healthia.AddPostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.*

class AddPostActivity : AppCompatActivity() {
    var firebaseAuth: FirebaseAuth? = null
    var userDbRef: DatabaseReference? = null
    var actionBar: ActionBar? = null

    //permission array
    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>

    //views
    lateinit var titleEt: EditText
    lateinit var descriptionEt: EditText
    lateinit var imageIv: ImageView
    lateinit var uploadBtn: Button

    //user info
    lateinit var name: String
    lateinit var email: String
    lateinit var uid: String
    lateinit var dp: String

    //info of post to be edited
    lateinit var editTitle: String
    lateinit var editDescription: String
    lateinit var editImage: String

    //image picked will be samed in this uri
    var image_uri: Uri? = null

    //progress bar
    var pd: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        actionBar = supportActionBar
        actionBar!!.title = "Add New Post"
        //enable back button in actionbar
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        //init permissions arrays
        cameraPermissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        pd = ProgressDialog(this)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUserStatus()

        //init views
        titleEt = findViewById(R.id.pTitleEt)
        descriptionEt = findViewById(R.id.pDescriptionEt)
        imageIv = findViewById(R.id.pImageIv)
        uploadBtn = findViewById(R.id.pUploadBtn)

        //get data throough intent from previous activity's adapter for edit
        val intent = intent
        val isUpdateKey = "" + intent.getStringExtra("key")
        val editPostId = "" + intent.getStringExtra("editPostId")
        //validate if we came here to update post i.e. came from postAdapter
        if (isUpdateKey == "editPost") {
            //update
            actionBar!!.setTitle("Update Post")
            uploadBtn.setText("Update")
            loadPostData(editPostId)
        } else {
            //add
            actionBar!!.setTitle("Add New Post")
            uploadBtn.setText("Upload")
        }

        actionBar!!.subtitle = email

        //get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance().getReference("Users")
        val query = userDbRef!!.orderByChild("email").equalTo(email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    name = "" + ds.child("name").value
                    email = "" + ds.child("email").value
                    dp = "" + ds.child("image").value
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })



        //get image from camera gallery on click
        imageIv.setOnClickListener(View.OnClickListener { ShowImagePickDialog() })

        //upload button click listener
        uploadBtn.setOnClickListener(View.OnClickListener { //get data (title, description) from EditText
            val title = titleEt.getText().toString().trim { it <= ' ' }
            val description = descriptionEt.getText().toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this@AddPostActivity, "Enter title...", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (TextUtils.isEmpty(description)) {
                Toast.makeText(this@AddPostActivity, "Enter description...", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            if (isUpdateKey == "editPost") {
                beginUpdate(title, description, editPostId)
            } else {
                uploadData(title, description)
            }
        })
    }

    private fun beginUpdate(title: String, description: String, editPostId: String) {
        pd!!.setMessage("Update Post...")
        pd!!.show()
        if (editImage != "noImage") {
            //was with image
            updateWasWithImage(title, description, editPostId)
        } else if (imageIv!!.drawable != null) {
            //was without image, but now has image in imageview
            updateWithNowImage(title, description, editPostId)
        } else {
            //without image and still no image in imageView
            updateWithoutImage(title, description, editPostId)
        }
        finish()
    }

    private fun updateWithoutImage(title: String, description: String, editPostId: String) {
        val hashMap = HashMap<String, Any?>()

        //put post info
        hashMap["uid"] = uid
        hashMap["uName"] = name
        hashMap["uEmail"] = email
        hashMap["uDp"] = dp
        hashMap["pTitle"] = title
        hashMap["pDescription"] = description
        hashMap["pImage"] = "noImage"
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        ref.child(editPostId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                pd!!.dismiss()
                Toast.makeText(this@AddPostActivity, "Updated...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                pd!!.dismiss()
                Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateWithNowImage(title: String, description: String, editPostId: String) {
        val timeStamp = System.currentTimeMillis().toString()
        val filePathAndName = "Posts/post_$timeStamp"

        //get image from imageView
        val bitmap = (imageIv!!.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                //image uploaded get it's url
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    //url is received, upload to firebase database
                    val hashMap = HashMap<String, Any?>()

                    //put post info
                    hashMap["uid"] = uid
                    hashMap["uName"] = name
                    hashMap["uEmail"] = email
                    hashMap["uDp"] = dp
                    hashMap["pTitle"] = title
                    hashMap["pDescription"] = description
                    hashMap["pImage"] = downloadUri
                    val ref = FirebaseDatabase.getInstance().getReference("Posts")
                    ref.child(editPostId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            pd!!.dismiss()
                            Toast.makeText(this@AddPostActivity, "Updated...", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener { e ->
                            pd!!.dismiss()
                            Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener { e ->
                pd!!.dismiss()
                Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateWasWithImage(title: String, description: String, editPostId: String) {
        //post is with image, delete previous image first
        val mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage!!)
        mPictureRef.delete()
            .addOnSuccessListener {
                //image deleted, upload new image
                //for post-image name, post-id, publish-time
                val timeStamp = System.currentTimeMillis().toString()
                val filePathAndName = "Posts/post_$timeStamp"

                //get image from imageView
                val bitmap = (imageIv!!.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                //image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val data = baos.toByteArray()
                val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
                ref.putBytes(data)
                    .addOnSuccessListener { taskSnapshot ->
                        //image uploaded get it's url
                        val uriTask = taskSnapshot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val downloadUri = uriTask.result.toString()
                        if (uriTask.isSuccessful) {
                            //url is received, upload to firebase database
                            val hashMap = HashMap<String, Any?>()

                            //put post info
                            hashMap["uid"] = uid
                            hashMap["uName"] = name
                            hashMap["uEmail"] = email
                            hashMap["uDp"] = dp
                            hashMap["pTitle"] = title
                            hashMap["pDescription"] = description
                            hashMap["pImage"] = downloadUri
                            val ref = FirebaseDatabase.getInstance().getReference("Posts")
                            ref.child(editPostId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener {
                                    pd!!.dismiss()
                                    Toast.makeText(
                                        this@AddPostActivity,
                                        "Updated...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    pd!!.dismiss()
                                    Toast.makeText(
                                        this@AddPostActivity,
                                        "" + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        pd!!.dismiss()
                        Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener { }
    }


    private fun loadPostData(editPostId: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Posts")
        //get detail of post using id of post
        val fquery = reference.orderByChild("pId").equalTo(editPostId)
        fquery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    //get data
                    editTitle = "" + ds.child("pTitle").value
                    editDescription = "" + ds.child("pDescription").value
                    editImage = "" + ds.child("pImage").value

                    //set data to views
                    titleEt!!.setText(editTitle)
                    descriptionEt!!.setText(editDescription)

                    //set image
                    if (editImage != "noImage") {
                        try {
                            Picasso.get().load(editImage).into(imageIv)
                        } catch (e: Exception) {
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun uploadData(title: String, description: String) {
        pd!!.setMessage("Publishing post...")
        pd!!.show()

        //for post-image name, post-id, post-publish-time
        val timeStamp = System.currentTimeMillis().toString()
        val filePathAndName = "Posts/post_$timeStamp"
        if (imageIv!!.drawable != null) {
            //get image from imageView
            val bitmap = (imageIv!!.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            //post with image
            val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
            ref.putBytes(data)
                .addOnSuccessListener { taskSnapshot ->
                    //image is uploaded to firebase storage, now get it's url
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri = uriTask.result.toString()
                    if (uriTask.isSuccessful) {
                        //url is received upload post to firebase
                        val hashMap = HashMap<Any, String?>()
                        //put post info
                        hashMap["uid"] = uid
                        hashMap["uName"] = name
                        hashMap["uEmail"] = email
                        hashMap["uDp"] = dp
                        hashMap["pId"] = timeStamp
                        hashMap["pTitle"] = title
                        hashMap["pDescription"] = description
                        hashMap["pImage"] = downloadUri
                        hashMap["pTime"] = timeStamp
                        hashMap["pLikes"] = "0"

                        //path to store post data
                        val ref = FirebaseDatabase.getInstance().getReference("Posts")

                        //put data in this ref
                        ref.child(timeStamp).setValue(hashMap)
                            .addOnSuccessListener {
                                pd!!.dismiss()
                                Toast.makeText(
                                    this@AddPostActivity,
                                    "Posts published",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //reset views
                                titleEt!!.setText("")
                                descriptionEt!!.setText("")
                                imageIv!!.setImageURI(null)
                                image_uri = null
                            }.addOnFailureListener { e -> //failed adding posts
                                pd!!.dismiss()
                                Toast.makeText(
                                    this@AddPostActivity,
                                    "" + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }.addOnFailureListener { e -> //failed upload image
                    pd!!.dismiss()
                    Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            //post without image
            val hashMap = HashMap<Any, String?>()
            hashMap["uid"] = uid
            hashMap["uName"] = name
            hashMap["uEmail"] = email
            hashMap["uDp"] = dp
            hashMap["pId"] = timeStamp
            hashMap["pTitle"] = title
            hashMap["pDescription"] = description
            hashMap["pImage"] = "noImage"
            hashMap["pTime"] = timeStamp
            hashMap["pLikes"] = "0"

            //path to store post data
            val ref = FirebaseDatabase.getInstance().getReference("Posts")

            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener {
                    pd!!.dismiss()
                    Toast.makeText(this@AddPostActivity, "Posts published", Toast.LENGTH_SHORT)
                        .show()
                    titleEt!!.setText("")
                    descriptionEt!!.setText("")
                    imageIv!!.setImageURI(null)
                    image_uri = null
                }.addOnFailureListener { e -> //failed adding posts
                    pd!!.dismiss()
                    Toast.makeText(this@AddPostActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
        }
        finish()
    }

    private fun ShowImagePickDialog() {
        //option(camera, gallery) to show in dialog
        val options = arrayOf("Camera", "Gallery")

        //dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image from")
        //set option to dialog
        builder.setItems(options) { dialogInterface, which ->
            //item click handle
            if (which == 0) {
                //camera clicked
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickFromCamera()
                }
            }
            if (which == 1) {
                //gallery clicked
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        }
        //create and show dialog
        builder.create().show()
    }

    private fun pickFromGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickFromCamera() {
        //intent to pick image from camera
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick")
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        //check if camera permission is enabled or not
        //return true if enabled
        //return false if not enabled
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        checkUserStatus()
    }

    override fun onResume() {
        super.onResume()
        checkUserStatus()
    }

    override fun onDestroy() {
        pd!!.dismiss()
        super.onDestroy()
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            email = user.email.toString()
            uid = user.uid
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(this, LoginOrRegisterActivity::class.java))
            finish()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //go to previous activity
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
//        menu.findItem(R.id.action_add_post).isVisible = false
        menu.findItem(R.id.action_search).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        }
        return super.onOptionsItemSelected(item)
    }

    //handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.size > 0) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted && storageAccepted) {
                    //both permission are granted
                    pickFromCamera()
                } else {
                    //camera or gallery or both permisson were denied
                    Toast.makeText(
                        this,
                        "Camera & Storage both permissions are necessary",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
            }
            STORAGE_REQUEST_CODE -> if (grantResults.size > 0) {
                val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeStorageAccepted) {
                    //storage permission granted
                    pickFromGallery()
                } else {
                    //storage permission denied
                    Toast.makeText(this, "Storage permission necessary", Toast.LENGTH_SHORT).show()
                }
            } else {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data!!.data

                //set to imageView
                imageIv!!.setImageURI(image_uri)
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image
                imageIv!!.setImageURI(image_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        //permission constants
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 200

        //image pick constants
        private const val IMAGE_PICK_CAMERA_CODE = 300
        private const val IMAGE_PICK_GALLERY_CODE = 400
    }
}