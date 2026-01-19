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
    fun getAnalisisCepat(): LiveData<List<AnalisisCepatModel>> {
        val liveData = MutableLiveData<List<AnalisisCepatModel>>()
        // Logic: Mengambil ringkasan laporan dari Firebase
        db.child(FirebaseConfig.PATH_OPERATIONAL).child("analisis_ringkasan")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(AnalisisCepatModel::class.java) }
                    liveData.postValue(list)
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