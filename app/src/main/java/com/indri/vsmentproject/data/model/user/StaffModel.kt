package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StaffModel(
    var id: String = "",
    val nama: String = "",
    val posisi: String = ""
) : Parcelable