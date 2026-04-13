package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "",          // manager / staff
    val posisi: String = "",
    val telepon: String = "",
    val foto_profil: String = "",
    val manager_id: String = "",
    val status: String = "aktif"
) : Parcelable