package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    var id: String = "",
    val tugas: String = "",
    val kategori: String = "",
    val staff_nama: String = "",
    val waktu_tenggat: String = "",
    val status: String = "pending",
    val keterangan: String = ""
) : Parcelable