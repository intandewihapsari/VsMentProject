package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class TugasModel(
    var id: String = "",
    var manager_id: String = "",
    var villa_id: String = "",
    var villa_nama: String = "",
    var ruangan: String = "",
    var staff_id: String = "",
    var staff_nama: String = "",
    var tugas: String = "",
    var deskripsi: String = "",
    var prioritas: String = "",
    var kategori: String = "",
    var deadline: String = "",
    var created_at: Long = System.currentTimeMillis(),
    var completed_at: Long = 0L,
    var status: String = "pending",
    var progress: Int = 0,
    var foto_tugas: String = "",
    var foto_staff: String = "",
    var bukti_foto: List<String> = emptyList(),

    // TAMBAHKAN INI BIAR SINKRON SAMA JSON DATABASE
    @get:PropertyName("is_validated")
    @set:PropertyName("is_validated")
    var is_validated: Boolean = false

) : Parcelable