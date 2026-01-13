package com.indri.vsmentproject.UI.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.Data.Model.LaporanModel

class LaporanViewModel : ViewModel() {

    private val _laporanList = MutableLiveData<List<LaporanModel>>()
    val laporanList: LiveData<List<LaporanModel>> = _laporanList

    fun getLaporanList() {
        FirebaseDatabase.getInstance().getReference("operational/laporan_barang")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        val laporan = it.getValue(LaporanModel::class.java)
                        laporan?.id = it.key ?: ""
                        laporan
                    }.reversed() // AGAR YANG TERBARU DI ATAS
                    _laporanList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateStatusLaporan(laporanId: String, statusBaru: String, onComplete: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("operational/laporan_barang")
            .child(laporanId).child("status_laporan").setValue(statusBaru)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}