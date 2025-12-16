package com.omurcansayla.fitrota.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "user",
    val isActive: Boolean = true
)