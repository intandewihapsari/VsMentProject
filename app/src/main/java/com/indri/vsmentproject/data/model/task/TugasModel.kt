package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class TugasModel(
    var id: String = "",
    val manager_id: String = "",       // Biar tau siapa yang ngasih tugas
    val villa_id: String = "",         // Link ke Villa tertentu
    val villa_nama: String = "",
    val staff_id: String = "",         // Link ke Staff tertentu
    val staff_nama: String = "",

    val tugas: String = "",
    val deskripsi: String = "",        // Ganti 'keterangan' biar lebih pro
    val kategori: String = "",         // "Pembersihan", "Perbaikan", dll
    val prioritas: String = "Normal",  // Tambahkan ini (Low, Normal, High)

    val waktu_tenggat: String = "",    // Konsisten pakai ini
    val status: String = "pending"     // "pending" atau "selesai"
) : Parcelable