package com.example.healthia.models

class ModelPost {
    //user same name as we given while uploading post
    var pId: String? = null
    var pTitle: String? = null
    var pDescription: String? = null
    var pLikes: String? = null
    var pImage: String? = null
    var pTime: String? = null
    var uid: String? = null
    var uEmail: String? = null
    var uDp: String? = null
    var uName: String? = null

    constructor() {}
    constructor(pId: String?, pTitle: String?, pDescription: String?, pLikes: String?, pImage: String?, pTime: String?, uid: String?, uEmail: String?, uDp: String?, uName: String?) {
        this.pId = pId
        this.pTitle = pTitle
        this.pDescription = pDescription
        this.pLikes = pLikes
        this.pImage = pImage
        this.pTime = pTime
        this.uid = uid
        this.uEmail = uEmail
        this.uDp = uDp
        this.uName = uName
    }
}