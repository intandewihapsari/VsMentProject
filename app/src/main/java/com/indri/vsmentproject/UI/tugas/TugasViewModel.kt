package com.indri.vsmentproject.UI.tugas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.Data.Model.TugasModel
import com.indri.vsmentproject.Data.Model.VillaModel
import com.indri.vsmentproject.Data.Model.StaffModel
import com.indri.vsmentproject.Data.Model.tugas.VillaTugasGroup

class TugasViewModel : ViewModel() {

    private val _tugasGrouped = MutableLiveData<List<VillaTugasGroup>>()
    val tugasGrouped: LiveData<List<VillaTugasGroup>> = _tugasGrouped

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    fun getVillaList() {
        FirebaseDatabase.getInstance().getReference("master_data/villa_list")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(VillaModel::class.java) }
                    _villaList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getStaffList() {
        FirebaseDatabase.getInstance().getReference("master_data/staff")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(StaffModel::class.java) }
                    _staffList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getTugasGroupedByVilla() {
        FirebaseDatabase.getInstance().getReference("operational/task_management")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupList = mutableListOf<VillaTugasGroup>()
                    for (villaSnapshot in snapshot.children) {
                        val tasks = villaSnapshot.child("tasks").children.mapNotNull { it.getValue(TugasModel::class.java) }
                        if (tasks.isNotEmpty()) groupList.add(VillaTugasGroup(villaSnapshot.key ?: "", tasks))
                    }
                    _tugasGrouped.postValue(groupList)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun simpanTugasLengkap(namaVilla: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("operational/task_management")
            .child(namaVilla).child("tasks").push().setValue(data)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}