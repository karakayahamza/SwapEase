package com.example.swapease.data.models

import android.graphics.Bitmap

data class Product(
    val publisherUid: String? = null,
    val productName: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val chats: List<Chat>? = null
) {
    constructor() : this("", "","","",chats = null)
}