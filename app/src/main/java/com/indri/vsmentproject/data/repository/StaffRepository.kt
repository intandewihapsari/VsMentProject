package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class StaffRepository {
    private val db = FirebaseDatabase.getInstance().reference

    // Fungsi untuk Dashboard: Analisis Cepat
    // StaffRepository.kt


    // Di dalam StaffRepository.kt
    fun getAnalisisCepat(villaId: String): LiveData<AnalisisCepatModel> {
        val liveData = MutableLiveData<AnalisisCepatModel>()

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ambil data sesuai path JSON yang kamu kirim
                val progress = snapshot.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
                    .child(villaId).child("summary/progress").value.toString()

                val laporanCount = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN).children.count {
                    it.child("villa_id").value == villaId
                }

                var rusakCount = 0
                val areas = snapshot.child(FirebaseConfig.PATH_VILLAS).child(villaId).child("areas")
                areas.children.forEach { area ->
                    area.child("items").children.forEach { item ->
                        if (item.child("kondisi").value.toString().equals("Rusak", ignoreCase = true)) {
                            rusakCount++
                        }
                    }
                }

                // Kirim SEBAGAI OBJEK TUNGGAL (Bukan List)
                liveData.postValue(AnalisisCepatModel(
                    progressTugas = if (progress == "null") "0%" else progress,
                    jumlahLaporan = laporanCount,
                    barangRusak = rusakCount
                ))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
    // Fungsi untuk Dashboard: Notifikasi Urgent
    fun getUrgentNotifications(): LiveData<List<NotifikasiModel>> {
        val liveData = MutableLiveData<List<NotifikasiModel>>()
        db.child(FirebaseConfig.PATH_NOTIFIKASI)
            .orderByChild("priority").equalTo("high") // Filter yang urgent saja
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