package com.indri.vsmentproject.ui.manager.task

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.model.villa.VillaModel
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

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _progresPerVilla = MutableLiveData<List<VillaTugasGroup>>()
    val progresPerVilla: LiveData<List<VillaTugasGroup>> = _progresPerVilla

    // ================================
    // FILTER
    // ================================
    fun filterTugas(status: String) {
        if (status == "All") {
            _tugasGrouped.postValue(rawGroups)
        } else {
            val filtered = rawGroups.map { group ->
                group.copy(
                    listTugas = group.listTugas.filter {
                        it.status.equals(status, ignoreCase = true)
                    }
                )
            }.filter { it.listTugas.isNotEmpty() }

            _tugasGrouped.postValue(filtered)
        }
    }

    // ================================
    // KATEGORI WAKTU
    // ================================
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

    // ================================
    // GROUP BY WAKTU
    // ================================
    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(s: DataSnapshot) {

                    val allTasks = s.children.flatMap { villaSnap ->
                        villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }
                    }

                    val groupedByTime = allTasks.groupBy {
                        getKategoriWaktu(it.deadline)
                    }

                    val finalGroups = groupedByTime.map { (waktu, tasks) ->
                        VillaTugasGroup(
                            villa_id = waktu,
                            namaVilla = waktu,
                            listTugas = tasks,
                            persentase_selesai = ""
                        )
                    }.sortedBy {
                        when (it.namaVilla) {
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

    // ================================
    // GROUP BY WAKTU (ALT)
    // ================================
    fun getTugasGroupedByWaktu() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(s: DataSnapshot) {

                    val allTasks = s.children.flatMap { villaSnap ->
                        villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }
                    }

                    val grouped = allTasks.groupBy {
                        getKategoriWaktu(it.deadline)
                    }

                    val groups = grouped.map { (waktu, tasks) ->
                        VillaTugasGroup(
                            villa_id = waktu,
                            namaVilla = waktu,
                            listTugas = tasks
                        )
                    }

                    _tugasGrouped.postValue(groups)
                }

                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // ================================
    // 🔥 PROGRESS PER VILLA (FINAL FIX)
    // ================================
    fun getTugasGroupedByVillaMurni() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(s: DataSnapshot) {

                    val groups = s.children.mapNotNull { villaSnap ->

                        // ❗ skip node yang bukan villa
                        if (!villaSnap.hasChild("list_tugas")) return@mapNotNull null

                        val tasks = villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }

                        if (tasks.isEmpty()) return@mapNotNull null

                        // 🔥 HITUNG SENDIRI
                        val total = tasks.size
                        val selesai = tasks.count {
                            it.status.equals("selesai", true)
                        }

                        val progressInt = if (total > 0) {
                            (selesai * 100) / total
                        } else 0

                        val persen = "$progressInt%"

                        val namaVilla = tasks.firstOrNull()?.villa_nama
                            ?: villaSnap.key ?: "Villa"

                        VillaTugasGroup(
                            villa_id = villaSnap.key ?: "",
                            namaVilla = namaVilla,
                            listTugas = tasks,
                            totalTugas = total,
                            tugasSelesai = selesai,
                            persentase_selesai = persen
                        )
                    }

                    _progresPerVilla.postValue(groups)
                }

                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // ================================
    // MASTER DATA
    // ================================
    fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    _staffList.postValue(
                        s.children.mapNotNull { it.getValue(UserModel::class.java) }
                    )
                }

                override fun onCancelled(e: DatabaseError) {}
            })
    }

    fun getVillaList() {
        db.child(FirebaseConfig.PATH_VILLAS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    _villaList.postValue(
                        s.children.mapNotNull {
                            it.getValue(VillaModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }
                    )
                }

                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // ================================
    // CRUD + AUTO SUMMARY
    // ================================
    private fun updateVillaSummary(villaId: String) {
        val path = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")

        path.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount.toInt()
                val completed = snapshot.children.count {
                    it.child("status").value.toString()
                        .equals("selesai", true)
                }

                val progress = if (total > 0) (completed * 100 / total) else 0

                db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
                    .child(villaId)
                    .child("summary")
                    .setValue(
                        mapOf(
                            "total" to total,
                            "completed" to completed,
                            "progress" to "$progress%"
                        )
                    )
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    fun simpanTugasLengkap(
        villaId: String,
        data: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")
            .push()

        val finalData = data.toMutableMap()
        finalData["id"] = ref.key ?: ""

        ref.setValue(finalData).addOnCompleteListener {
            if (it.isSuccessful) {
                updateVillaSummary(villaId)
            }
            onComplete(it.isSuccessful)
        }
    }

    fun updateTugas(
        villaId: String,
        taskId: String,
        data: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")
            .child(taskId)
            .updateChildren(data)
            .addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }

    fun hapusTugas(
        villaId: String,
        taskId: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")
            .child(taskId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) updateVillaSummary(villaId)
                onComplete(it.isSuccessful)
            }
    }
}