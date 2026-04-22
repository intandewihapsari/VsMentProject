package com.indri.vsmentproject.ui.manager.masterdata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.repository.NotificationRepository
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class DataViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _riwayatNotif = MutableLiveData<List<NotifikasiModel>>()
    val riwayatNotif: LiveData<List<NotifikasiModel>> = _riwayatNotif

    private var originalNotifList: List<NotifikasiModel> = listOf()

    private val notifRepo = NotificationRepository()

    // Ambil data dari folder "notifikasi" (Sesuaikan jika di Firebase namanya berbeda)
    // Di dalam DataViewModel.kt

    fun getRiwayatInstruksi(uid: String) {
        // Gunakan fungsi dari repo yang sudah kita benerin tadi
        notifRepo.getMyNotifications(uid).observeForever { resource ->
            when (resource) {
                is Resource.Success -> {
                    val list = resource.data ?: emptyList()
                    originalNotifList = list
                    _riwayatNotif.postValue(list)
                }
                is Resource.Error -> {
                    Log.e("DATA_VIEWMODEL", "Error: ${resource.message}")
                }
                else -> {}
            }
        }
    }
    fun filterNotif(filterType: String) {
        if (originalNotifList.isEmpty()) return

        val filteredList = when (filterType) {
            "Pending" -> originalNotifList.filter { !it.is_read }
            "Selesai" -> originalNotifList.filter { it.is_read }
            else -> originalNotifList
        }
        _riwayatNotif.postValue(filteredList)
    }

    // --- Master Data Villa & Staff ---
    fun getData() {
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                }
                _villaList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.child(FirebaseConfig.PATH_STAFFS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(UserModel::class.java)?.apply { uid = it.key ?: "" }
                }
                _staffList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // CRUD Ops
    fun hapusStaff(uid: String) = db.child(FirebaseConfig.PATH_STAFFS).child(uid).removeValue()
    fun simpanStaff(uid: String, data: Map<String, Any>) = db.child(FirebaseConfig.PATH_STAFFS).child(uid).updateChildren(data)
    fun simpanVilla(id: String, data: Map<String, Any>) = db.child(FirebaseConfig.PATH_VILLAS).child(id).updateChildren(data)
    fun hapusVilla(id: String) = db.child(FirebaseConfig.PATH_VILLAS).child(id).removeValue()
}