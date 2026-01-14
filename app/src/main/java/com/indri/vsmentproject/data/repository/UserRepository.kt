package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*

class UserRepository {
    private val db = FirebaseDatabase.getInstance().reference

    fun getVillaList(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        db.child("master_data/villa_list").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                liveData.postValue(snapshot.children.map { it.child("nama").value.toString() })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    fun getStaffList(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        db.child("master_data/staff").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                liveData.postValue(snapshot.children.map { it.child("nama").value.toString() })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
}