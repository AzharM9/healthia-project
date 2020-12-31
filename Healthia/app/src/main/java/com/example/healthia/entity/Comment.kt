package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(
    var commentId: Int = 0,
    var commentContent: String = "",
    var userRefId: Int = 0
): Parcelable
