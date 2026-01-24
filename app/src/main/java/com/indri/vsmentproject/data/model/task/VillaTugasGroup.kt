package com.indri.vsmentproject.data.model.task

data class VillaTugasGroup(
    val villa_id: String = "",
    val namaVilla: String = "",
    val listTugas: List<TugasModel>,
    val totalTugas: Int = 0,
    val tugasSelesai: Int = 0,
    val persentase_selesai: String = "0%", // Tambahkan ini untuk progress bar di dashboard
    var isExpanded: Boolean = false
)
