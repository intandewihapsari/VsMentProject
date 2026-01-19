package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifikasiModel(
    val id: String = "",             // Gunakan "id" agar konsisten dengan model lain
    val judul: String = "",
    val pesan: String = "",
    val tipe: String = "info",       // "urgent", "info", "warning"
    val status_baca: Boolean = false,
    val waktu: String = "",          // Format: yyyy-MM-dd HH:mm

    // Routing Notifikasi
    val target_uid: String = "",     // UID spesifik penerima (Manager/Staff tertentu)
    val target_role: String = "",    // Jika notif ditujukan ke semua "manager"
    val sender_id: String = "",      // Siapa yang mengirim (Contoh: UID Staff)

    val villa_id: String = "",       // ID Villa terkait untuk navigasi klik
    val villa_nama: String = ""
) : Parcelable