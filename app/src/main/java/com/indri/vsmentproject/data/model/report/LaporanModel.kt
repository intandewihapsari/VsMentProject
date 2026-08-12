package com.indri.vsmentproject.data.model.report

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LaporanModel(
    // ID & Timestamp
    var id: String = "",                          // ID laporan
    var created_at: Long = 0L,                    // Unix timestamp

    // Relasi Data
    var villa_id: String = "",
    var villa_nama: String = "",
    var area: String = "",                        // Sesuai JSON baru: area (bukan ruangan)

    var staff_id: String = "",
    var staff_nama: String = "",

    // Konten Laporan
    var tipe_laporan: String = "",                // Habis / Rusak / Hilang
    var nama_barang: String = "",                 // Sesuai JSON baru: nama_barang (bukan item_nama)
    var deskripsi: String = "",

    // Prioritas
    var prioritas: String = "Normal",             // Low / Normal / Urgent

    // Bukti Foto
    var foto_url: String = "",

    // Status Workflow
    var status: String = "pending",               // pending / proses / selesai / ditolak
    var catatan_manager: String = "",

    // Waktu
    var waktu_lapor: String = "",                 // yyyy-MM-dd HH:mm
    var waktu_selesai: String = ""
) : Parcelable