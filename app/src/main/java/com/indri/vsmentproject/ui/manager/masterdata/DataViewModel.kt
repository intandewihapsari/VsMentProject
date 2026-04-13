package com.indri.vsmentproject.ui.manager.masterdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class DataViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    // =========================
    // VILLA
    // =========================
    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    // =========================
    // STAFF (pakai UserModel)
    // =========================
    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    // =========================
    // RIWAYAT NOTIF / INSTRUKSI
    // =========================
    private val _riwayatNotif = MutableLiveData<List<LaporanModel>>()
    val riwayatNotif: LiveData<List<LaporanModel>> = _riwayatNotif

    // =========================
    // GET SEMUA DATA
    // =========================
    fun getData() {

        // ===== VILLA =====
        db.child(FirebaseConfig.PATH_VILLAS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(VillaModel::class.java)?.apply {
                            id = it.key ?: ""
                        }
                    }
                    _villaList.postValue(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // ===== STAFF =====
        db.child(FirebaseConfig.PATH_STAFFS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(UserModel::class.java)?.apply {
                            uid = it.key ?: ""
                        }
                    }
                    _staffList.postValue(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // GET RIWAYAT INSTRUKSI
    // =========================
    fun getRiwayatInstruksi(managerUid: String) {

        db.child(FirebaseConfig.PATH_NOTIFIKASI)
            .orderByChild("sender_id")
            .equalTo(managerUid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = snapshot.children.mapNotNull { snap ->

                        val judul = snap.child("judul").value?.toString() ?: "-"
                        val tipe = snap.child("tipe").value?.toString() ?: "info"
                        val villa = snap.child("villa_nama").value?.toString() ?: "Umum"

                        val statusText = when (tipe) {
                            "urgent" -> "Urgent"
                            "warning" -> "Perhatian"
                            else -> "Terkirim"
                        }

                        LaporanModel(
                            id = snap.key ?: "",
                            nama_barang = judul,
                            tipe_laporan = "Instruksi",
                            status = statusText,
                            villa_nama = villa
                        )
                    }

                    _riwayatNotif.postValue(list.reversed())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // CRUD STAFF
    // =========================
    fun hapusStaff(uid: String) {
        db.child(FirebaseConfig.PATH_STAFFS).child(uid).removeValue()
    }

    fun simpanStaff(uid: String, data: Map<String, Any>) {
        db.child(FirebaseConfig.PATH_STAFFS).child(uid).updateChildren(data)
    }

    // =========================
    // CRUD VILLA
    // =========================
    fun simpanVilla(id: String, data: Map<String, Any>) {
        db.child(FirebaseConfig.PATH_VILLAS).child(id).updateChildren(data)
    }

    fun hapusVilla(id: String) {
        db.child(FirebaseConfig.PATH_VILLAS).child(id).removeValue()
    }
}