package com.indri.vsmentproject.data.model.notification

data class NotifikasiModel(
    val id_notifikasi: String = "",
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "", // "urgent" atau "info"
    val status_baca: Boolean = false,
    val waktu: String = "",
    val ditujukan_ke: String = "",
    val villa_terkait: String = ""
)