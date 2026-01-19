package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalisisCepatModel(
    val id: String = "",
    val judul: String = "",
    val nilai: Int = 0,        // Berubah jadi Int agar mudah dijumlahkan
    val keterangan: String = "",
    val tipe: String = "",     // Contoh: "villa", "staff", "laporan"
    val warna_aksen: String = "" // Opsional: Untuk menentukan warna card di UI
) : Parcelable