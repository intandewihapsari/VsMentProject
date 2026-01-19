package com.indri.vsmentproject.data.model.user

data class User(
    val uid: String? = null,
    val nama: String? = null,
    val email: String? = null,
    val role: String? = null, // "manager" atau "staff"
    val posisi: String? = null
)