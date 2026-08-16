package com.indri.vsmentproject.data.repository

import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

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

    // --- 3. UPDATE (Ubah Status Laporan) ---
    fun updateStatus(managerId: String, laporanId: String, status: String, onComplete: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any>(
            FirebaseConfig.FIELD_STATUS to status
        )
        if (status == "selesai") {
            updates["waktu_selesai"] = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        }

        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .child(laporanId)
            .updateChildren(updates)
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