package com.example.swapease.data.models

import android.graphics.Bitmap

data class Product(
    val sellerUid: String? = null,
    val productName: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
) {
    // No-argument (parametresiz) kurucu metod
    constructor() : this("", "","")
}