package com.indri.vsmentproject.data.model.inventory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InventarisModel(
    var id: String = "",
    var nama_barang: String = "",
    var villa_id: String = "",
    var kondisi: String = "",          // "Bagus", "Rusak", "Hilang"

    // Untuk Ringkasan Dashboard (Summary)
    var total_rusak: Int = 0,
    var total_hilang: Int = 0,
    var total_habis: Int = 0
) : Parcelable