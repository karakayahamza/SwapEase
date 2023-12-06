package com.example.swapease.data.models

import com.google.firebase.firestore.PropertyName

class Message (
       @get:PropertyName("senderUid") // veya @PropertyName("senderUid")
       val senderUid: String = "",
       @get:PropertyName("receiverUid") // veya @PropertyName("receiverUid")
       val receiverUid: String = "",
       @get:PropertyName("text") // veya @PropertyName("text")
       val text: String = "",
       @get:PropertyName("timestamp") // veya @PropertyName("timestamp")
       val timestamp: Long = 0

)