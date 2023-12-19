package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val userProfileImage : String?
): Parcelable {
    constructor() : this("", "", "", null)
}