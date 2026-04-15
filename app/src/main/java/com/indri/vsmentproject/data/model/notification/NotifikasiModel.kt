package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifikasiModel(

    // IDENTITAS
    var id: String = "",

    // KONTEN NOTIF
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "info", // "urgent", "info", "warning"


    @get:PropertyName("is_read")
    @set:PropertyName("is_read")
    var is_read: Boolean = false,

    // WAKTU
    val waktu: String = "",          // Format: yyyy-MM-dd HH:mm
    val timestamp: Long = 0,         // Untuk sorting realtime

    // TARGET (PENERIMA)
    val target_uid: String = "",     // Kirim ke user tertentu
    val target_role: String = "",    // Kirim ke semua role (contoh: "staff")

    // PENGIRIM
    val sender_id: String = "",

    // KONTEKS (NAVIGASI)
    val villa_id: String = "",
    val villa_nama: String = "",

    // USER (OPSIONAL - BACKWARD COMPATIBLE)
    val user_id: String = ""

) : Parcelable