package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class StaffRepository {

    // --- READ: Dashboard Analisis Cepat (Dinamis per Manager & Villa) ---
    fun getAnalisisCepat(managerId: String, villaId: String): LiveData<AnalisisCepatModel> {
        val liveData = MutableLiveData<AnalisisCepatModel>()

        // PERBAIKAN: Gunakan Manager Root Ref langsung
        val managerRef = FirebaseConfig.getManagerRef(managerId)

        managerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Ambil summary progress tugas villa dari: task_management/{villaId}/summary/progress
                val progress = snapshot.child(FirebaseConfig.CHILD_TASK_MANAGEMENT)
                    .child(villaId)
                    .child("summary")
                    .child("progress").value.toString()

                // 2. Hitung jumlah TOTAL laporan kerusakan di villa ini
                val laporanCount = snapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.count {
                    it.child("villa_id").value?.toString() == villaId
                }

                // 3. Hitung barang yang statusnya masih "pending" di villa ini
                val rusakCount = snapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.count {
                    it.child("villa_id").value?.toString() == villaId &&
                            it.child(FirebaseConfig.FIELD_STATUS).value?.toString()?.equals(FirebaseConfig.STATUS_PENDING, ignoreCase = true) == true
                }

                liveData.postValue(AnalisisCepatModel(
                    progressTugas = if (progress == "null") "0%" else progress,

                ))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    // --- READ: Notifikasi Urgent ---
    fun getUrgentNotifications(managerId: String): LiveData<List<NotifikasiModel>> {
        val liveData = MutableLiveData<List<NotifikasiModel>>()

        FirebaseConfig.getNotifikasiRef(managerId)
            .orderByChild("tipe")
            .equalTo("urgent")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(NotifikasiModel::class.java) }
                    liveData.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return liveData
    }
}