package com.indri.vsmentproject.ui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel

class DataViewModel : ViewModel() {

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    fun getData() {
        val ref = FirebaseDatabase.getInstance().getReference("master_data")

        // Ambil Villa
        ref.child("villa_list").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _villaList.postValue(s.children.mapNotNull {
                    val v = it.getValue(VillaModel::class.java)
                    v?.id = it.key ?: ""
                    v
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        // Ambil Staff
        ref.child("staff").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _staffList.postValue(s.children.mapNotNull {
                    val st = it.getValue(StaffModel::class.java)
                    st?.id = it.key ?: ""
                    st
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // CRUD VILLA
    fun simpanVilla(id: String?, data: Map<String, Any>) {
        val ref = FirebaseDatabase.getInstance().getReference("master_data/villa_list")
        if (id == null) ref.push().setValue(data) else ref.child(id).setValue(data)
    }

    fun hapusVilla(id: String) {
        FirebaseDatabase.getInstance().getReference("master_data/villa_list").child(id).removeValue()
    }

    // CRUD STAFF
    fun simpanStaff(id: String?, data: Map<String, Any>) {
        val ref = FirebaseDatabase.getInstance().getReference("master_data/staff")
        if (id == null) ref.push().setValue(data) else ref.child(id).setValue(data)
    }

    fun hapusStaff(id: String) {
        FirebaseDatabase.getInstance().getReference("master_data/staff").child(id).removeValue()
    }
}