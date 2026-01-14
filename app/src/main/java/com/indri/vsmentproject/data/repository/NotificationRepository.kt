package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel

class NotificationRepository {
    private val db = FirebaseDatabase.getInstance().reference

    fun getAnalisisCepat(): LiveData<List<AnalisisCepatModel>> {
        val liveData = MutableLiveData<List<AnalisisCepatModel>>()

        // Kita tembak ke task_management karena datanya ada di sana
        db.child("operational/task_management").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalTugas = 0
                var totalRusak = 0

                for (villa in snapshot.children) {
                    val tasks = villa.child("tasks")
                    totalTugas += tasks.childrenCount.toInt()

                    for (task in tasks.children) {
                        val kategori = task.child("kategori").value.toString()
                        if (kategori.contains("Perbaikan", ignoreCase = true) || kategori.contains("Rusak", ignoreCase = true)) {
                            totalRusak++
                        }
                    }
                }

                // Kita bungkus jadi model supaya bisa dibaca ViewHolder
                val hasilAnalisis = AnalisisCepatModel(
                    judul = "Tugas",
                    nilai = totalTugas.toString(), // Akan muncul di jml_laporan
                    keterangan = totalRusak.toString() // Akan muncul di jml_barangRusak
                )
                liveData.postValue(listOf(hasilAnalisis))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    fun getUrgentNotifications(): LiveData<List<NotifikasiModel>> {
        val liveData = MutableLiveData<List<NotifikasiModel>>()
        db.child("operational/notifikasi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(NotifikasiModel::class.java) }
                liveData.postValue(list.filter { it.tipe == "urgent" })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
}