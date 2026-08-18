package com.indri.vsmentproject.data.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class TugasModel(
    var id: String = "",

    @get:PropertyName("manager_id")
    @set:PropertyName("manager_id")
    var manager_id: String = "",

    @get:PropertyName("villa_id")
    @set:PropertyName("villa_id")
    var villa_id: String = "",

    @get:PropertyName("villa_nama")
    @set:PropertyName("villa_nama")
    var villa_nama: String = "",

    var ruangan: String = "",

    @get:PropertyName("staff_id")
    @set:PropertyName("staff_id")
    var staff_id: String = "",

    @get:PropertyName("staff_nama")
    @set:PropertyName("staff_nama")
    var staff_nama: String = "",

    var tugas: String = "",
    var deskripsi: String = "",
    var prioritas: String = "",
    var kategori: String = "",
    var deadline: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var created_at: Long = System.currentTimeMillis(),

    @get:PropertyName("completed_at")
    @set:PropertyName("completed_at")
    var completed_at: Long = 0L,

    var status: String = "pending",
    var progress: Int = 0,

    @get:PropertyName("foto_tugas")
    @set:PropertyName("foto_tugas")
    var foto_tugas: String = "",

    @get:PropertyName("foto_staff")
    @set:PropertyName("foto_staff")
    var foto_staff: String = "",

    @get:PropertyName("bukti_foto")
    @set:PropertyName("bukti_foto")
    var bukti_foto: List<String>? = emptyList(), // Bukti foto bisa null di Firebase

    @get:PropertyName("is_validated")
    @set:PropertyName("is_validated")
    var is_validated: Boolean = false

) : Parcelable