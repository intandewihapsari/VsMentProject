package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StaffModel(
    val id: String = "",      // Pastikan konsisten menggunakan 'id' atau 'uid'
    val nama: String = "",
    val posisi: String = "",
    val email: String = "",
    val foto_profil: String = ""
) : Parcelable