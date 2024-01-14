package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Message(
       val senderUid: String,
       val senderUserName : String,
       val receiverUid: String,
       val text: String,
       val timestamp: Long,
       var messageId: String?,

):Parcelable{
       // Add a no-argument constructor
       constructor() : this("","","", "", 0L,null)
}