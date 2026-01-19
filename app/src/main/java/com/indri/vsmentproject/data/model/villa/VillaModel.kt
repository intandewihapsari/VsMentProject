package com.indri.vsmentproject.data.model.villa

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VillaModel(
    var id: String = "",              // ID unik dari Firebase Push Key
    val manager_id: String = "",      // Menandakan siapa pemilik/pengelola villa ini
    val nama: String = "",
    val alamat: String = "",          // Menambah detail lokasi
    val deskripsi: String = "",       // Penjelasan singkat tentang villa
    val area: List<String> = emptyList(), // Daftar ruangan (Dapur, Kamar, dll)
    val foto: String = "",            // URL Cloudinary
    val status_tersedia: Boolean = true // Untuk menandai villa aktif atau sedang renovasi
) : Parcelable