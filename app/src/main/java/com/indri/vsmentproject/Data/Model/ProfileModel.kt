package com.indri.vsmentproject.Data.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileModel(
    val id: String = "",
    val nama: String = "Manager Admin",
    val email: String = "manager@villas.com",
    val jabatan: String = "General Manager",
    val foto: String = ""
) : Parcelable