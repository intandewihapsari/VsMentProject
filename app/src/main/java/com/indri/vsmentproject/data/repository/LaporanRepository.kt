package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class LaporanRepository {

    // --- 1. CREATE (Tambah Laporan - Biasanya dipakai oleh Staff) ---
    fun createLaporan(managerId: String, laporan: LaporanModel, onComplete: (Boolean, String?) -> Unit) {
        // PERBAIKAN: Menggunakan helper baru yang mengarah tepat ke operational/laporan_kerusakan anaknya
        val laporanRef = FirebaseConfig.getLaporanKerusakanRef(managerId)

        // Push untuk generate ID otomatis di Firebase
        val newLaporanKey = laporanRef.push().key
        if (newLaporanKey == null) {
            onComplete(false, "Gagal membuat ID Laporan")
            return
        }

        // Set ID ke dalam objek laporan
        val laporanWithId = laporan.apply { id = newLaporanKey }

        laporanRef.child(newLaporanKey).setValue(laporanWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // --- 2. READ (Ambil Semua Laporan) ---
    fun getAllLaporan(managerId: String): LiveData<Resource<List<LaporanModel>>> {
        val liveData = MutableLiveData<Resource<List<LaporanModel>>>()
        liveData.postValue(Resource.Loading())

        // PERBAIKAN: Samakan menggunakan helper referensi anaknya
        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(LaporanModel::class.java)?.apply { id = it.key ?: "" }
                    }.reversed()
                    liveData.postValue(Resource.Success(list))
                }
                override fun onCancelled(error: DatabaseError) {
                    liveData.postValue(Resource.Error(error.message))
                }
            })
        return liveData
    }

    // --- 3. UPDATE (Ubah Status Laporan) ---
    fun updateStatus(managerId: String, laporanId: String, status: String, onComplete: (Boolean) -> Unit) {
        // PERBAIKAN: Langsung tuju ke ID laporan di dalam sub-folder yang benar
        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .child(laporanId)
            .child(FirebaseConfig.FIELD_STATUS)
            .setValue(status)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // --- 3. UPDATE (Ubah Catatan Manager) ---
    fun updateCatatan(managerId: String, laporanId: String, catatan: String, onComplete: (Boolean) -> Unit) {
        // PERBAIKAN: Jalur updateChildren disesuaikan ke lokasi anaknya
        val updates = mapOf("catatan_manager" to catatan)

        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .child(laporanId)
            .updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // --- 4. DELETE (Hapus Laporan jika diperlukan) ---
    fun deleteLaporan(managerId: String, laporanId: String, onComplete: (Boolean) -> Unit) {
        // PERBAIKAN: Target penghapusan langsung pada node anak yang pas
        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .child(laporanId)
            .removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}