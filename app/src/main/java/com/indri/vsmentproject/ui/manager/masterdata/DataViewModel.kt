package com.indri.vsmentproject.ui.manager.masterdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.villa.VillaModel
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
    // STAFF
    // =========================
    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    // =========================
    // RIWAYAT INSTRUKSI (PAKAI NOTIF)
    // =========================
    private val _riwayatNotif = MutableLiveData<List<NotifikasiModel>>()
    val riwayatNotif: LiveData<List<NotifikasiModel>> = _riwayatNotif

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
    // GET RIWAYAT INSTRUKSI (DARI NOTIF)
    // =========================
    fun getRiwayatInstruksi(managerUid: String) {

        db.child(FirebaseConfig.PATH_NOTIFIKASI)
            .orderByChild("sender_id")
            .equalTo(managerUid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<NotifikasiModel>()

                    for (snap in snapshot.children) {

                        val notif = snap.getValue(NotifikasiModel::class.java)

                        notif?.let {
                            it.id = snap.key ?: ""
                            list.add(it)
                        }
                    }

                    _riwayatNotif.postValue(
                        list.sortedByDescending { it.timestamp }
                    )
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