package com.indri.vsmentproject.data.model.report

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LaporanModel(
    var id: String = "",
    val villa_nama: String = "",
    val area: String = "",
    val staff_nama: String = "",
    val jenis_laporan: String = "", // "rusak" atau "habis"
    val nama_barang: String = "",
    val keterangan: String = "",
    var status_laporan: String = "belum_ditindaklanjuti",
    val waktu_lapor: String = "",
    val prioritas: String = ""
) : Parcelable