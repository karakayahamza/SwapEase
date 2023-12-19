package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Message(
       val senderUid: String,
       val receiverUid: String,
       val text: String,
       val timestamp: Long
):Parcelable{
       // Add a no-argument constructor
       constructor() : this("", "", "", 0L)
}