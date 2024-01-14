package com.example.swapease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String?,
    val username: String?,
    val email: String?,
    val userProfileImage : String?,
    val completedSwapes: Int?,
    val rating : Double?,
): Parcelable {
    constructor() : this("", "", "", null,0,0.0)
}