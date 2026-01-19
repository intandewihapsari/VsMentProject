package com.indri.vsmentproject.data.model.cloudinary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Model untuk menampung data respon setelah berhasil upload ke Cloudinary.
 * Digunakan agar data yang diterima dari API Cloudinary lebih terstruktur.
 */
@Parcelize
data class CloudinaryResponseModel(
    val public_id: String = "",    // ID unik file di Cloudinary (berguna untuk hapus foto)
    val secure_url: String = "",   // URL gambar dengan protokol HTTPS
    val format: String = "",       // Format file (jpg, png, dll)
    val created_at: String = "",   // Waktu upload
    val bytes: Int = 0             // Ukuran file dalam bytes
) : Parcelable