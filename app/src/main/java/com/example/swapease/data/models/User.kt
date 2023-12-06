package com.example.swapease.data.models

data class User(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val products: List<Product>? = null
)