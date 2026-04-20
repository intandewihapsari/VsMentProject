package com.indri.vsmentproject.data.model.inventory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InventarisModel(
    val id: String = "",
    val nama_barang: String = "",
    val villa_id: String = "",
    val kondisi: String = "",          // "Bagus", "Rusak", "Hilang"

    // Untuk Ringkasan Dashboard (Summary)
    val total_rusak: Int = 0,
    val total_hilang: Int = 0,
    val total_habis: Int = 0
) : Parcelable