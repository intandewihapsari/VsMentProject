package com.indri.vsmentproject.Data.Model

data class NotifikasiModel(
    val id_notifikasi: String = "",
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "",
    val status_baca: Boolean = false
)

