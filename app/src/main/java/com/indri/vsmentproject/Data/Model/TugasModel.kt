package com.indri.vsmentproject.Data.Model


data class TugasModel(
    val tugas: String = "",
    val keterangan: String = "",
    val kategori: String = "",     // Tambahkan ini untuk filter & detail
    val deadline: String = "",     // Tambahkan ini untuk info batas waktu
    val status: String = "",       // "pending" atau "selesai"
    val staff_nama: String = ""
)
