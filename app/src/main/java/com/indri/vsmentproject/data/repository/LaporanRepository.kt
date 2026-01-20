package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class LaporanRepository {
    // Menggunakan path dari Config
    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

    fun getAllLaporan(): LiveData<Resource<List<LaporanModel>>> {
        val liveData = MutableLiveData<Resource<List<LaporanModel>>>()
        liveData.postValue(Resource.Loading())

        // addValueEventListener membuat dashboard Manager otomatis update saat Staff kirim laporan
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(LaporanModel::class.java)?.apply { id = it.key ?: "" }
                }.reversed() // Laporan terbaru di atas
                liveData.postValue(Resource.Success(list))
            }
            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(Resource.Error(error.message))
            }
        })
        return liveData
    }
    fun updateStatus(laporanId: String, status: String, onComplete: (Boolean) -> Unit) {
        // Konsisten menggunakan FIELD_STATUS dari config
        db.child(laporanId).child(FirebaseConfig.FIELD_STATUS).setValue(status)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}