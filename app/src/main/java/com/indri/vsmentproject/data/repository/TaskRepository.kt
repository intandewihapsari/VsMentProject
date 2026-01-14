package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.InventarisModel

class TaskRepository {
    private val db = FirebaseDatabase.getInstance().getReference("operational/task_management")

    fun getAllPendingTasks(): LiveData<List<TugasModel>> {
        val liveData = MutableLiveData<List<TugasModel>>()
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allTasks = mutableListOf<TugasModel>()
                for (villaSnapshot in snapshot.children) {
                    villaSnapshot.child("tasks").children.forEach { taskSnapshot ->
                        val task = taskSnapshot.getValue(TugasModel::class.java)
                        if (task?.status == "pending") {
                            task.id = taskSnapshot.key ?: ""
                            allTasks.add(task)
                        }
                    }
                }
                liveData.postValue(allTasks)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    fun getInventarisSummary(): LiveData<InventarisModel> {
        val liveData = MutableLiveData<InventarisModel>()
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ganti = 0; var periksa = 0; var layak = 0
                for (villa in snapshot.children) {
                    for (task in villa.child("tasks").children) {
                        val status = task.child("status").value.toString()
                        val kategori = task.child("kategori").value.toString()
                        if (status == "selesai") layak++
                        else if (kategori == "Perbaikan") ganti++ else periksa++
                    }
                }
                liveData.postValue(InventarisModel(ganti, periksa, layak))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
}