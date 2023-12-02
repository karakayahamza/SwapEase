package com.example.swapease.data.models

class User (
    val uid: String = "",
    val username: String = "",
    val email: String = ""
){
    enum class UserType {
        BUYER, SELLER
    }
}