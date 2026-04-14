package com.indri.vsmentproject.data.model.villa

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VillaModel(
    var id: String = "",
    val manager_id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val deskripsi: String = "",

    val area: List<String> = emptyList(),   // ✅ ikut Firebase
    val fasilitas: List<String> = emptyList(),

    val foto_villa: String = "",            // ✅ ikut Firebase
    val status_tersedia: Boolean = true
) : Parcelable