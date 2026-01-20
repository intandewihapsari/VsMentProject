package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StaffModel(
    var uid: String = "",
    val nama: String = "",
    val email: String = "",
    val posisi: String = "", // Tambahkan ini agar tidak error di Fragment
    val role: String = "staff",
    val status: String = "aktif"
) : Parcelable