package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    var articleId: Int = 0,
    var articleTitle: String = "",
    var articlePhoto: String = "",
    var articleContent: String = "",
    var userRefId: Int = 0

): Parcelable
