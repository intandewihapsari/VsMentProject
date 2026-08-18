package com.indri.vsmentproject.data.model.report

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class LaporanModel(
    var id: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var created_at: Long = 0L,

    @get:PropertyName("villa_id")
    @set:PropertyName("villa_id")
    var villa_id: String = "",

    @get:PropertyName("villa_nama")
    @set:PropertyName("villa_nama")
    var villa_nama: String = "",

    var area: String = "",

    @get:PropertyName("staff_id")
    @set:PropertyName("staff_id")
    var staff_id: String = "",

    @get:PropertyName("staff_nama")
    @set:PropertyName("staff_nama")
    var staff_nama: String = "",

    @get:PropertyName("tipe_laporan")
    @set:PropertyName("tipe_laporan")
    var tipe_laporan: String = "",

    @get:PropertyName("nama_barang")
    @set:PropertyName("nama_barang")
    var nama_barang: String = "",

    var deskripsi: String = "",
    var prioritas: String = "Normal",

    @get:PropertyName("foto_url")
    @set:PropertyName("foto_url")
    var foto_url: String = "",

    @get:PropertyName("bukti_foto")
    @set:PropertyName("bukti_foto")
    var bukti_foto: List<String> = emptyList(),

    var status: String = "pending",

    @get:PropertyName("catatan_manager")
    @set:PropertyName("catatan_manager")
    var catatan_manager: String = "",

    @get:PropertyName("waktu_lapor")
    @set:PropertyName("waktu_lapor")
    var waktu_lapor: String = "",

    @get:PropertyName("waktu_selesai")
    @set:PropertyName("waktu_selesai")
    var waktu_selesai: String = ""
) : Parcelable