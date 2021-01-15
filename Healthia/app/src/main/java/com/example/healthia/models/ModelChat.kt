package com.example.healthia.models

import com.google.firebase.database.PropertyName

class ModelChat {
    var message: String? = null
    var receiver: String? = null
    var sender: String? = null
    var timestamp: String? = null

    @get:PropertyName("isSeen")
    @set:PropertyName("isSeen")
    var isSeen = false

    constructor() {}
    constructor(message: String?, receiver: String?, sender: String?, timestamp: String?, isSeen: Boolean) {
        this.message = message
        this.receiver = receiver
        this.sender = sender
        this.timestamp = timestamp
        this.isSeen = isSeen
    }
}