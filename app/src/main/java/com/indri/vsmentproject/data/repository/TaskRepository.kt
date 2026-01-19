package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.inventory.InventarisModel

class TaskRepository {
    private val db = FirebaseDatabase.getInstance().reference.child("operational")

    fun getPendingTasks(villaId: String? = null): LiveData<List<TugasModel>> {
        val liveData = MutableLiveData<List<TugasModel>>()

        db.child("task_management").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allTasks = mutableListOf<TugasModel>()
                for (villaSnapshot in snapshot.children) {
                    // Jika kita butuh filter per villa
                    if (villaId != null && villaSnapshot.key != villaId) continue

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

    // Menghitung statistik inventaris dengan lebih akurat
    fun getInventarisStats(): LiveData<InventarisModel> {
        val liveData = MutableLiveData<InventarisModel>()
        db.child("laporan_kerusakan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var rusak = 0; var proses = 0; var selesai = 0
                for (report in snapshot.children) {
                    val status = report.child("status_laporan").value.toString()
                    when (status) {
                        "rusak" -> rusak++
                        "proses" -> proses++
                        "selesai" -> selesai++
                    }
                }
                liveData.postValue(InventarisModel(rusak, proses, selesai))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }
}