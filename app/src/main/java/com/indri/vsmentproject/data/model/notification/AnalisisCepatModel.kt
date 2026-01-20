package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalisisCepatModel(
    val progressTugas: String = "0%", // Sesuai JSON: operational/task_management/summary/progress
    val jumlahLaporan: Int = 0,      // Hasil hitung dari operational/laporan_kerusakan
    val barangRusak: Int = 0         // Hasil hitung kondisi 'Rusak' di master_data/villas
) : Parcelable