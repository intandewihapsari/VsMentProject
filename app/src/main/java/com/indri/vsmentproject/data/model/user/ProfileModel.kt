package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileModel(
    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val jabatan: String = "",
    val foto: String = ""
) : Parcelable