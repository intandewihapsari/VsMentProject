package com.indri.vsmentproject.ui.manager.masterdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class DataViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    private val _riwayatNotif = MutableLiveData<List<LaporanModel>>()
    val riwayatNotif: LiveData<List<LaporanModel>> = _riwayatNotif

    fun getData() {
        // Ambil Data Villa
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _villaList.postValue(s.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        // Ambil Data Staff
        db.child(FirebaseConfig.PATH_STAFFS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _staffList.postValue(s.children.mapNotNull {
                    it.getValue(StaffModel::class.java)?.apply { uid = it.key ?: "" }
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    fun getRiwayatInstruksi(managerUid: String) {
        db.child(FirebaseConfig.PATH_NOTIFIKASI)
            .orderByChild("sender_id")
            .equalTo(managerUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val list = s.children.mapNotNull {
                        val notif = it.getValue(NotifikasiModel::class.java)
                        LaporanModel(
                            id = it.key ?: "",
                            nama_barang = notif?.judul ?: "Instruksi",
                            tipe_laporan = "Instruksi",
                            status = if (notif?.tipe == "urgent") "Urgent" else "Terkirim",
                            villa_nama = notif?.villa_nama ?: "Umum"
                        )
                    }
                    _riwayatNotif.postValue(list.reversed())
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }
    // Di dalam DataViewModel.kt
    fun simpanVilla(id: String, data: Map<String, Any>) {
        // Menggunakan updateChildren agar data lama tidak terhapus total
        db.child(FirebaseConfig.PATH_VILLAS).child(id).updateChildren(data)
            .addOnSuccessListener { /* Berhasil */ }
    }

    fun simpanStaff(uid: String, data: Map<String, Any>) {
        // Mengarah ke PATH_STAFFS (users/staffs)
        db.child(FirebaseConfig.PATH_STAFFS).child(uid).updateChildren(data)
    }
    fun hapusVilla(id: String) { db.child(FirebaseConfig.PATH_VILLAS).child(id).removeValue() }
    fun hapusStaff(id: String) { db.child(FirebaseConfig.PATH_STAFFS).child(id).removeValue() }
}