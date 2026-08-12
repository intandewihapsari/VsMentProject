package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifikasiModel(
    // IDENTITAS
    var id: String = "",

    // KONTEN NOTIF
    var judul: String = "",
    var pesan: String = "",
    var tipe: String = "info",       // urgent / info / warning

    // Menggunakan JvmField agar anotasi PropertyName Firebase bekerja sempurna pada tipe Boolean
    @get:PropertyName("is_read")
    @set:PropertyName("is_read")
    var is_read: Boolean = false,

    // WAKTU
    var waktu: String = "",          // Format: yyyy-MM-dd HH:mm
    var timestamp: Long = 0L,        // Untuk sorting realtime

    // TARGET (PENERIMA)
    var target_uid: String = "",     // Kirim ke user tertentu
    var target_role: String = "",    // Kirim ke semua role (contoh: "staff")

    // PENGIRIM
    var sender_id: String = "",

    // KONTEKS (NAVIGASI)
    var villa_id: String = "",
    var villa_nama: String = "",

    // USER (OPSIONAL - BACKWARD COMPATIBLE)
    var user_id: String = ""
) : Parcelable