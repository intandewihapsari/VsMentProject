package com.indri.vsmentproject.ui.manager.task

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import java.text.SimpleDateFormat
import java.util.*

class TugasViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().reference

    private var rawGroups = listOf<VillaTugasGroup>()

    private val _tugasGrouped = MutableLiveData<List<VillaTugasGroup>>()
    val tugasGrouped: LiveData<List<VillaTugasGroup>> = _tugasGrouped

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

    private fun getKategoriWaktu(deadline: String): String {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateDeadline = sdf.parse(deadline)

            val now = Calendar.getInstance()
            val today = sdf.format(now.time)

            now.add(Calendar.DATE, 1)
            val tomorrow = sdf.format(now.time)

            when (deadline) {
                today -> "Hari Ini"
                tomorrow -> "Besok"
                else -> {
                    val dateToday = sdf.parse(today)
                    if (dateDeadline != null && dateDeadline.before(dateToday)) "Terlambat"
                    else "Mendatang"
                }
            }
        } catch (e: Exception) {
            "Mendatang"
        }
    }

    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                // 1. Ambil semua tugas dari semua villa
                val allTasks = s.children.flatMap { villaSnap ->
                    villaSnap.child("list_tugas").children.mapNotNull {
                        it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                    }
                }

                // 2. Kelompokkan berdasarkan Waktu (Hari Ini, Besok, Mendatang)
                val groupedByTime = allTasks.groupBy { getKategoriWaktu(it.deadline) }

                // 3. Di dalam setiap waktu, kelompokkan lagi berdasarkan Nama Villa
                val finalGroups = groupedByTime.map { (waktu, tasksDiWaktuItu) ->
                    // Kita gunakan model VillaTugasGroup tapi 'namaVilla' diisi label Waktu
                    // Dan listTugas tetap berisi semua tugas di waktu tersebut
                    VillaTugasGroup(
                        villa_id = waktu,
                        namaVilla = waktu, // Header Utama: "Hari Ini"
                        listTugas = tasksDiWaktuItu,
                        persentase_selesai = ""
                    )
                }.sortedBy {
                    when(it.namaVilla) {
                        "Hari Ini" -> 1
                        "Besok" -> 2
                        else -> 3
                    }
                }

                rawGroups = finalGroups
                _rawGroupsLive.postValue(finalGroups)
                _tugasGrouped.postValue(finalGroups)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // --- UNTUK DAFTAR TUGAS (Berdasarkan Waktu) ---
    fun getTugasGroupedByWaktu() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val allTasks = s.children.flatMap { villaSnap ->
                    villaSnap.child("list_tugas").children.mapNotNull {
                        it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                    }
                }
                val groupedByTime = allTasks.groupBy { getKategoriWaktu(it.deadline) }
                val groups = groupedByTime.map { (waktu, tasks) ->
                    VillaTugasGroup(waktu, waktu, tasks, persentase_selesai = "")
                }.sortedBy { /* urutan waktu */ 1 }

                _tugasGrouped.postValue(groups)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // --- UNTUK PROGRESS DETAIL (Murni Per Villa) ---
    private val _progresPerVilla = MutableLiveData<List<VillaTugasGroup>>()
    val progresPerVilla: LiveData<List<VillaTugasGroup>> = _progresPerVilla

    fun getTugasGroupedByVillaMurni() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val groups = s.children.mapNotNull { villaSnap ->
                    val tasks = villaSnap.child("list_tugas").children.mapNotNull {
                        it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                    }
                    val progress = villaSnap.child("summary/progress").value?.toString() ?: "0%"
                    // Ambil nama villa asli dari data pertama atau key-nya
                    val realName = tasks.firstOrNull()?.villa_nama ?: villaSnap.key ?: ""

                    if (tasks.isNotEmpty()) {
                        VillaTugasGroup(
                            villa_id = villaSnap.key ?: "",
                            namaVilla = realName, // Ini akan memunculkan Nama Villa asli
                            listTugas = tasks,
                            persentase_selesai = progress
                        )
                    } else null
                }
                _progresPerVilla.postValue(groups)
            }
            override fun onCancelled(e: DatabaseError) {}
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
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }
}