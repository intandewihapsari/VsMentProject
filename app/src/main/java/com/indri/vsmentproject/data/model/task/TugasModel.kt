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
    val worker_id: String = "",        // Pastikan ini 'worker_id'
    val worker_name: String = "",
    val tugas: String = "",
    val deskripsi: String = "",
    val prioritas: String = "Medium",
    val kategori: String = "Umum",
    val deadline: String = "",
    val created_at: Long = System.currentTimeMillis(),
    val status: String = "pending",
    val completed_at: Long = 0L, // Tambahkan field ini
) : Parcelable