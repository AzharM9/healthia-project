package com.example.healthia.models

class ModelChatlist {
    var id // we will need this id to get chat list, sender/receiver  uid
            : String? = null

    constructor() {}
    constructor(id: String?) {
        this.id = id
    }
}