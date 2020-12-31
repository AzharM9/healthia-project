package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Feedback(
    var feedbackId: Int = 0,
    var feedbackContent: String = "",
    var userRefId: Int = 0
): Parcelable
