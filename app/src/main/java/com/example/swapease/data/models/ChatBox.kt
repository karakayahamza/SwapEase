package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatBox(
    val chatBoxId: String
) :Parcelable{
    // Add a no-argument constructor
    constructor() : this("")
}