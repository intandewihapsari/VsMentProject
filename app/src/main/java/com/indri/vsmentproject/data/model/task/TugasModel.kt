package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    var id: String = "",
    val tugas: String = "",
    val kategori: String = "",
    val staff_nama: String = "",
    val waktu_tenggat: String = "", // Gunakan satu nama ini saja, jangan dicampur 'deadline'
    val status: String = "pending", // "pending" atau "selesai"
    val keterangan: String = ""
) : Parcelable