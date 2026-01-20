package com.indri.vsmentproject.ui.manager.task

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class TugasViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().reference

    // Data asli untuk filter lokal
    private var rawGroups = listOf<VillaTugasGroup>()

    // LiveData khusus LIST (Bisa berubah saat difilter)
    private val _tugasGrouped = MutableLiveData<List<VillaTugasGroup>>()
    val tugasGrouped: LiveData<List<VillaTugasGroup>> = _tugasGrouped

    // LiveData khusus RINGKASAN ATAS (Data Asli & Tetap)
    private val _rawGroupsLive = MutableLiveData<List<VillaTugasGroup>>()
    val rawGroupsLive: LiveData<List<VillaTugasGroup>> = _rawGroupsLive

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    fun filterTugas(status: String) {
        if (status == "All") {
            _tugasGrouped.postValue(rawGroups)
        } else {
            val filtered = rawGroups.map { group ->
                group.copy(
                    listTugas = group.listTugas.filter { it.status.equals(status, ignoreCase = true) }
                )
            }.filter { it.listTugas.isNotEmpty() }
            _tugasGrouped.postValue(filtered)
        }
    }

    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val groups = s.children.mapNotNull { villaSnap ->
                    val tasks = villaSnap.child("list_tugas").children.mapNotNull {
                        it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                    }
                    val progress = villaSnap.child("summary/progress").value?.toString() ?: "0%"
                    val realName = tasks.firstOrNull()?.villa_nama ?: villaSnap.key ?: ""

                    if (tasks.isNotEmpty()) {
                        VillaTugasGroup(
                            villa_id = villaSnap.key ?: "",
                            namaVilla = realName,
                            listTugas = tasks,
                            persentase_selesai = progress
                        )
                    } else null
                }
                rawGroups = groups
                _rawGroupsLive.postValue(groups) // Update ringkasan atas
                _tugasGrouped.postValue(groups)   // Update list bawah
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // ... (Fungsi simpanTugasLengkap, updateTugas, hapusTugas tetap sama) ...
    fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _staffList.postValue(s.children.mapNotNull { it.getValue(StaffModel::class.java) })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    fun getVillaList() {
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                _villaList.postValue(s.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun updateVillaSummary(villaId: String) {
        val path = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas")
        path.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount.toInt()
                val completed = snapshot.children.count {
                    it.child("status").value.toString().equals("selesai", ignoreCase = true)
                }
                val progress = if (total > 0) (completed * 100 / total) else 0

                db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("summary").setValue(
                    mapOf("total" to total, "completed" to completed, "progress" to "$progress%")
                )
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    fun simpanTugasLengkap(villaId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val ref = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").push()
        val finalData = data.toMutableMap()
        finalData["id"] = ref.key ?: ""

        ref.setValue(finalData).addOnCompleteListener {
            if (it.isSuccessful) {
                updateVillaSummary(villaId)
                db.child(FirebaseConfig.PATH_NOTIFIKASI).child(data["worker_id"].toString()).push().setValue(
                    mapOf(
                        "judul" to "Tugas Baru",
                        "pesan" to "Tugas: ${data["tugas"]} di ${data["ruangan"]}",
                        "is_read" to false,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            onComplete(it.isSuccessful)
        }
    }

    fun updateTugas(villaId: String, taskId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .updateChildren(data).addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }

    fun hapusTugas(villaId: String, taskId: String, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseDatabase.getInstance().reference.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId).path.toString())
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }
}