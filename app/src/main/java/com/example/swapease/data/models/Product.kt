package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId : String? = null,
    val publisherUid: String? = null,
    val publisherName: String?,
    val productName: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val addedDate: String? = null
):Parcelable {
    constructor() : this(productId = null, "","","","",null,"")
}