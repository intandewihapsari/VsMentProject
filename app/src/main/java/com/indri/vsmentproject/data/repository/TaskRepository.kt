package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class TaskRepository {
    private val db = FirebaseDatabase.getInstance().reference

    // SINKRON: Mengembalikan Resource tunggal untuk Summary Dashboard
    fun getInventarisSummary(): LiveData<Resource<InventarisModel>> {
        val liveData = MutableLiveData<Resource<InventarisModel>>()
        liveData.postValue(Resource.Loading())

        db.child(FirebaseConfig.PATH_OPERATIONAL).child("inventaris_summary")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(InventarisModel::class.java)
                    liveData.postValue(Resource.Success(data ?: InventarisModel()))
                }
                override fun onCancelled(error: DatabaseError) {
                    liveData.postValue(Resource.Error(error.message))
                }
            })
        return liveData
    }

    // SINKRON: Mengembalikan Resource List untuk Dashboard
    fun getAllPendingTasks(): LiveData<Resource<List<TugasModel>>> {
        val liveData = MutableLiveData<Resource<List<TugasModel>>>()
        // Looping semua villa untuk cari tugas pending
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allPending = mutableListOf<TugasModel>()
                snapshot.children.forEach { villa ->
                    villa.child("list_tugas").children.forEach { tugasSnap ->
                        val tugas = tugasSnap.getValue(TugasModel::class.java)
                        if (tugas?.status?.equals("pending", true) == true) {
                            allPending.add(tugas.apply { id = tugasSnap.key ?: "" })
                        }
                    }
                }
                liveData.postValue(Resource.Success(allPending))
            }
            override fun onCancelled(e: DatabaseError) {
                liveData.postValue(Resource.Error(e.message))
            }
        })
        return liveData
    }
    // Fungsi bawaan kamu tetap dipertahankan
    fun getVillaList(onResult: (DataSnapshot) -> Unit) {
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) = onResult(snapshot)
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveTaskWithNotification(namaVilla: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val taskRef = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(namaVilla).child("tasks").push()
        taskRef.setValue(data).addOnCompleteListener { taskResult ->
            if (taskResult.isSuccessful) {
                val staffNama = data["staff_nama"].toString()
                val notifData = mapOf(
                    "judul" to "Tugas Baru: $namaVilla",
                    "pesan" to "Kamu mendapat tugas baru: ${data["tugas"]}",
                    "waktu" to System.currentTimeMillis(),
                    "status" to "unread"
                )
                db.child(FirebaseConfig.PATH_NOTIFIKASI).child(staffNama).push().setValue(notifData)
            }
            onComplete(taskResult.isSuccessful)
        }
    }
}