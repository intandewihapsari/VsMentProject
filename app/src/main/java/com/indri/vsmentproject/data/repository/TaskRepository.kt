package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.task.VillaTugasGroup

class TaskRepository {
    private val db = FirebaseDatabase.getInstance()

    // Ambil data Tugas Grouped
    fun getGroupedTasks(): LiveData<List<VillaTugasGroup>> {
        val liveData = MutableLiveData<List<VillaTugasGroup>>()
        db.getReference("operational/task_management")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groups = mutableListOf<VillaTugasGroup>()
                    // ... logika mapping ...
                    liveData.postValue(groups)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return liveData
    }
}