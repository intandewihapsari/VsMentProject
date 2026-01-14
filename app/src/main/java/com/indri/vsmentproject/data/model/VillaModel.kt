package com.indri.vsmentproject.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VillaModel(
    var id: String = "",
    val nama: String = "",
    val area: List<String> = emptyList(),
    val foto: String = ""
) : Parcelable