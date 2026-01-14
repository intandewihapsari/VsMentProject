package com.indri.vsmentproject.ui.tugas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.tugas.TugasModel
import com.indri.vsmentproject.data.model.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.model.tugas.VillaTugasGroup

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
                        val tasks = villaSnapshot.child("tasks").children.mapNotNull {
                            val task = it.getValue(TugasModel::class.java)
                            task?.id = it.key ?: "" // MENYIMPAN ID DARI FIREBASE
                            task
                        }
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

    fun updateTugas(namaVilla: String, taskId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("operational/task_management")
            .child(namaVilla).child("tasks").child(taskId).updateChildren(data)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun hapusTugas(namaVilla: String, taskId: String, onComplete: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("operational/task_management")
            .child(namaVilla)
            .child("tasks")
            .child(taskId)
            .removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}