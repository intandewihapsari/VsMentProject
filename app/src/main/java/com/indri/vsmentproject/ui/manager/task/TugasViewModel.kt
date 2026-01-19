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

    fun getVillaList() {
        // Gunakan FirebaseConfig agar konsisten dengan Dashboard
        db.child(FirebaseConfig.PATH_VILLAS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(VillaModel::class.java) }
                    _villaList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getStaffList() {
        // Ambil hanya yang role-nya staff agar Manager tidak salah pilih
        db.child(FirebaseConfig.PATH_USERS).orderByChild("role").equalTo("staff")
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
                        // Ambil key (nama villa)
                        val villaName = villaSnapshot.key ?: "Villa Tidak Diketahui"

                        // Ambil list tugas di dalam child "tasks"
                        val tasks = villaSnapshot.child("tasks").children.mapNotNull { taskSnapshot ->
                            taskSnapshot.getValue(TugasModel::class.java)?.apply {
                                id = taskSnapshot.key ?: "" // Pastikan ID terisi dari key Firebase
                            }
                        }

                        // Hanya masukkan ke list jika ada tugasnya
                        if (tasks.isNotEmpty()) {
                            // Pastikan nama parameter sesuai dengan data class: namaVilla dan listTugas
                            groupList.add(VillaTugasGroup(namaVilla = villaName, listTugas = tasks))
                        }
                    }
                    _tugasGrouped.postValue(groupList)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Tambahkan log error agar mudah debugging saat sidang
                }
            })
    }
    fun simpanTugasLengkap(namaVilla: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val taskRef = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(namaVilla).child("tasks").push()

        taskRef.setValue(data).addOnCompleteListener { taskResult ->
            if (taskResult.isSuccessful) {
                // LOGIKA NOTIFIKASI SEMPURNA: Kirim ke Staff terkait
                val staffNama = data["staff_nama"].toString()
                val notifData = mapOf(
                    "judul" to "Tugas Baru di $namaVilla",
                    "pesan" to "Manager memberikan tugas: ${data["tugas"]}",
                    "waktu" to System.currentTimeMillis(),
                    "status" to "unread"
                )
                db.child(FirebaseConfig.PATH_NOTIFIKASI).child(staffNama).push().setValue(notifData)
            }
            onComplete(taskResult.isSuccessful)
        }
    }

    fun updateTugas(namaVilla: String, taskId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(namaVilla).child("tasks").child(taskId).updateChildren(data)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun hapusTugas(namaVilla: String, taskId: String, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(namaVilla).child("tasks").child(taskId)
            .removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}