package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    var id: String = "",
    val manager_id: String = "",
    val villa_id: String = "",
    val villa_nama: String = "",
    val ruangan: String = "Umum",

    val staff_id: String = "",
    val staff_name: String = "",

    val tugas: String = "",
    val deskripsi: String = "",
    val prioritas: String = "Medium",
    val kategori: String = "Umum",
    val deadline: String = "",

    val created_at: Long = System.currentTimeMillis(),
    val status: String = "pending",
    val completed_at: Long = 0L,

    val staff_photo: String = ""
) : Parcelable
data class VillaGroup(
    val namaVilla: String,
    val listTugas: List<TugasModel>
)

data class WaktuContainer(
    val kategoriWaktu: String,
    val listVilla: List<VillaTugasGroup> // Pastikan namanya persis begini
)