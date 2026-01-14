package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    var id: String = "",           // Tambahkan baris ini
    val tugas: String = "",
    val kategori: String = "",
    val staff_nama: String = "",
    val deadline: String = "",
    val status: String = "",
    val keterangan: String = ""
) : Parcelable