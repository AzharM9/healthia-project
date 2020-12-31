package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Forum(
    var forumId: Int = 0,
    var forumTitle: String = "",
    var forumPhoto: String = "",
    var forumContent: String = "",
    var userRefId: Int = 0

): Parcelable
