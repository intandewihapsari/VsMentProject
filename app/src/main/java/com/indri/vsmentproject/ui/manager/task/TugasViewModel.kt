package com.indri.vsmentproject.ui.manager.task

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class TugasViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().reference

    private val _tugasGrouped = MutableLiveData<List<VillaTugasGroup>>()
    val tugasGrouped: LiveData<List<VillaTugasGroup>> = _tugasGrouped

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    // Menghitung ulang persentase secara otomatis
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


    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val groups = s.children.mapNotNull { villaSnap ->
                    // 1. Ambil list_tugas secara dinamis
                    val tasks = villaSnap.child("list_tugas").children.mapNotNull {
                        it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                    }

                    // 2. Ambil data summary untuk persentase
                    val progress = villaSnap.child("summary/progress").value?.toString() ?: "0%"
                    // Jika ingin nama villa yang asli (bukan ID), ambil dari salah satu tugas atau master_data
                    val realName = tasks.firstOrNull()?.villa_nama ?: villaSnap.key ?: ""

                    // 3. Masukkan ke group (SINKRONKAN SEMUA PARAMETER)
                    if (tasks.isNotEmpty()) {
                        VillaTugasGroup(
                            villa_id = villaSnap.key ?: "", // Mengambil V01, V02, dst
                            namaVilla = realName,
                            listTugas = tasks,
                            persentase_selesai = progress
                        )
                    } else null
                }
                _tugasGrouped.postValue(groups)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    fun simpanTugasLengkap(villaId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val ref = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").push()
        ref.setValue(data).addOnCompleteListener {
            if (it.isSuccessful) {
                updateVillaSummary(villaId)
                // Notifikasi dinamis berdasarkan worker_id
                db.child(FirebaseConfig.PATH_NOTIFIKASI).child(data["worker_id"].toString()).push().setValue(
                    mapOf(
                        "judul" to "Tugas Baru",
                        "pesan" to "Tugas: ${data["tugas"]}",
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
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }
}