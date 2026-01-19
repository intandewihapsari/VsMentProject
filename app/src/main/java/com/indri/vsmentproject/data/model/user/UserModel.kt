package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "",     // "manager" atau "staff"
    val posisi: String = "",   // Pengganti 'jabatan' agar konsisten dengan field registrasi
    val foto_profil: String = "", // URL Cloudinary
    val manager_id: String = "", // Jika user ini adalah staff, simpan ID managernya di sini
    val telepon: String = ""
) : Parcelable