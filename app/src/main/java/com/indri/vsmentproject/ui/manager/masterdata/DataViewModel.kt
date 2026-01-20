package com.indri.vsmentproject.ui.manager.masterdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class DataViewModel : ViewModel() {
    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    private val db = FirebaseDatabase.getInstance().reference

    fun getData() {
        // Ambil Villa dari master_data/villas
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _villaList.postValue(s.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        // Ambil Staff dari users (Filter role staff)
        db.child(FirebaseConfig.PATH_USERS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val list = s.children.mapNotNull {
                    it.getValue(StaffModel::class.java)?.apply { uid = it.key ?: "" }
                }
                _staffList.postValue(list.filter { it.role == "staff" })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // CRUD VILLA
    fun simpanVilla(id: String?, data: Map<String, Any>) {
        val ref = db.child(FirebaseConfig.PATH_VILLAS)
        if (id == null) ref.push().setValue(data) else ref.child(id).updateChildren(data)
    }

    fun hapusVilla(id: String) {
        db.child(FirebaseConfig.PATH_VILLAS).child(id).removeValue()
    }

    // CRUD STAFF (Mengarah ke users)
    fun simpanStaff(id: String?, data: Map<String, Any>) {
        val ref = db.child(FirebaseConfig.PATH_USERS)
        if (id == null) ref.push().setValue(data) else ref.child(id).updateChildren(data)
    }

    fun hapusStaff(id: String) {
        db.child(FirebaseConfig.PATH_USERS).child(id).removeValue()
    }
}