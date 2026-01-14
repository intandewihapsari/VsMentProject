package com.indri.vsmentproject.data.model.notifikasi

data class NotifikasiModel(
    val id_notifikasi: String = "",
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "",
    val status_baca: Boolean = false
)