package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskTemplateModel(
    var id: String = "",
    var nama_template: String = "",                 // Contoh: "SOP Kebersihan Kamar Tidur"
    var deskripsi: String = "",                     // Contoh: "Standar kebersihan kamar harian"
    var list_tugas_item: List<String> = emptyList() // Contoh: ["Sapu & Pel", "Ganti Sprei", "Cek AC"]
) : Parcelable