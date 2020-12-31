package com.example.healthia.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PanicBroadcast(
   var broadCastId: Int = 0,
   var broadcastPermission: Boolean = false,
   var broadcastMessage: String = "",
   var userRefId: Int = 0

): Parcelable
