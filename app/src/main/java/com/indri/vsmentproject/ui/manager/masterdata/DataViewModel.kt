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
    private val notifRepo = NotificationRepository()

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _riwayatNotif = MutableLiveData<List<NotifikasiModel>>()
    val riwayatNotif: LiveData<List<NotifikasiModel>> = _riwayatNotif

    private var originalNotifList: List<NotifikasiModel> = listOf()

    // =============================
    // NOTIFIKASI
    // =============================
    fun getRiwayatInstruksi(managerId: String) {
        notifRepo.getMyNotifications(managerId, managerId, isManager = true).observeForever { resource ->
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

    // =============================
    // GET DATA VILLA & STAFF (SINKRON JSON BARU: master_data)
    // =============================
    fun getData(managerId: String) {
        // Ambil Data Villa (villa_management/{managerId}/master_data/villas)
        FirebaseConfig.getVillasRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                    }
                    _villaList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Ambil Data Staff (villa_management/{managerId}/master_data/staffs)
        FirebaseConfig.getStaffsRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(UserModel::class.java)?.apply { uid = it.key ?: "" }
                    }
                    _staffList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =============================
    // STAFF CRUD
    // =============================
    fun simpanStaff(managerId: String, staffUid: String, data: Map<String, Any>) {
        FirebaseConfig.getStaffsRef(managerId).child(staffUid).updateChildren(data)
    }

    fun hapusStaff(managerId: String, staffUid: String) {
        FirebaseConfig.getStaffsRef(managerId).child(staffUid).removeValue()
    }

    // =============================
    // VILLA CRUD
    // =============================
    fun simpanVilla(managerId: String, villaId: String, villa: VillaModel) {
        FirebaseConfig.getVillasRef(managerId).child(villaId).setValue(villa)
    }

    fun hapusVilla(managerId: String, villaId: String) {
        FirebaseConfig.getVillasRef(managerId).child(villaId).removeValue()
    }
}