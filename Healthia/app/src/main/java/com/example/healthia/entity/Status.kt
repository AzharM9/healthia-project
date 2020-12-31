package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Status(
    var statusId: Int = 0,
    var statusPhoto: String = "",
    var statusContent: String = "",
    var userRefId: Int = 0
): Parcelable
