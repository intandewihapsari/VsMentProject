package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalisisCepatModel(
    val id: String = "",
    val judul: String = "",
    val nilai: String = "",
    val keterangan: String = "",
    val tipe: String = ""
) : Parcelable