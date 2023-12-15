package com.example.swapease.data.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId : String? = null,
    val publisherUid: String? = null,
    val productName: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val chats: List<Chat>? = null
):Parcelable {
    constructor() : this(productId = null, "","","","",chats = null)
}