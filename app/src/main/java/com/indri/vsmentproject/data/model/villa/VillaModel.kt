package com.indri.vsmentproject.data.model.villa

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class VillaModel(
    var id: String = "",
    var manager_id: String = "",
    var nama: String = "",
    var alamat: String = "",
    var deskripsi: String = "",
    var area: List<String> = listOf(),
    var fasilitas: List<String> = listOf(),
    var foto_villa: String = "",
    var status_tersedia: Boolean = true
) : Parcelable
