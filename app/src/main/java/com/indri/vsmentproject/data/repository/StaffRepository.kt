package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.user.StaffModel

class StaffRepository {
    private val db = FirebaseDatabase.getInstance().getReference("users")

    fun getStaffListByManager(managerUid: String): LiveData<List<StaffModel>> {
        val liveData = MutableLiveData<List<StaffModel>>()

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<StaffModel>()
                for (data in snapshot.children) {
                    val model = data.getValue(StaffModel::class.java)
                    // Filter: Hanya ambil staff yang didaftarkan oleh Manager ini
                    if (model?.role == "staff" && model.manager_id == managerUid) {
                        list.add(model)
                    }
                }
                liveData.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
}