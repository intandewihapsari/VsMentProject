package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.notifikasi.NotifikasiModel

class MainRepository {

    fun loadNotifikasi(): LiveData<List<NotifikasiModel>> {
        val liveData = MutableLiveData<List<NotifikasiModel>>()
        val ref = FirebaseDatabase.getInstance()
            .getReference("operational/notifikasi")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotifikasiModel>()
                for (child in snapshot.children) {
                    child.getValue(NotifikasiModel::class.java)
                        ?.let { list.add(it) }
                }
                liveData.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return liveData
    }

}
