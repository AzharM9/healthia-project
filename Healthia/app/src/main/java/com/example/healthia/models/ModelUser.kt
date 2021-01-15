package com.example.healthia.models

class ModelUser {
    //use same name as in firebase databasse
    var name: String? = null
    var email: String? = null
    var search: String? = null
    var phone: String? = null
    var image: String? = null
    var cover: String? = null
    var uid: String? = null
    var onlineStatus: String? = null
    var typingTo: String? = null

    constructor() {}
    constructor(name: String?, email: String?, search: String?, phone: String?, image: String?, cover: String?, uid: String?, onlineStatus: String?, typingTo: String?) {
        this.name = name
        this.email = email
        this.search = search
        this.phone = phone
        this.image = image
        this.cover = cover
        this.uid = uid
        this.onlineStatus = onlineStatus
        this.typingTo = typingTo
    }
}