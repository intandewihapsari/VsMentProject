package com.indri.vsmentproject.Data.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StaffModel(
    var id: String = "",
    val nama: String = "",
    val posisi: String = ""
) : Parcelable