package com.indri.vsmentproject.data.model.notification

data class AnalisisCepatModel(
    val id_notifikasi: String = "",
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "",
    val status_baca: Boolean = false,
    val waktu: String = "",
    val villa_terkait: String = ""
)