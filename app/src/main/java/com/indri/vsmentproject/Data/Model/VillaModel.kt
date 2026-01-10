package com.indri.vsmentproject.Data.Model

data class VillaModel(
    val id: String = "",
    val nama: String = "", // Sesuaikan dengan JSON
    val foto: String = "", // Sesuaikan dengan JSON
    val area: List<String> = listOf()
)