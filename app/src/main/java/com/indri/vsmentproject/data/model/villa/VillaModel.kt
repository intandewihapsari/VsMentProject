package com.indri.vsmentproject.data.model.villa

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class VillaModel(
    var id: String = "",

    @get:PropertyName("manager_id")
    @set:PropertyName("manager_id")
    var manager_id: String = "",

    var nama: String = "",
    var alamat: String = "",
    var deskripsi: String = "",
    var area: List<String> = listOf(),
    var fasilitas: List<String> = listOf(),

    @get:PropertyName("foto_villa")
    @set:PropertyName("foto_villa")
    var foto_villa: String = "",

    @get:PropertyName("status_tersedia")
    @set:PropertyName("status_tersedia")
    var status_tersedia: Boolean = true
) : Parcelable