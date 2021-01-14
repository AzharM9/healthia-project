package com.example.firebaseapp

data class ModelPost (
    //user same name as we given while uploading post
    var pId: String? = "",
    var pTitle: String? = "",
    var pDescription: String? = "",
    var pImage: String? = "",
    var pTime: String? = "",
    var uid: String? = "",
    var uEmail: String? = "",
    var uDp: String? = "",
    var uName: String? = "",
)
