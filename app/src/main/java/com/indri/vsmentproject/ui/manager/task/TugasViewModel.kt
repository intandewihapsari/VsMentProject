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
    // FILTER (FIX: lebih aman)
    // ================================
    fun filterTugas(status: String) {
        if (status == "All") {
            _tugasGrouped.postValue(rawGroups)
            return
        }

        val filtered = rawGroups.mapNotNull { group ->
            val filteredTasks = group.listTugas.filter {
                it.status.equals(status, true)
            }

            if (filteredTasks.isEmpty()) null
            else group.copy(listTugas = filteredTasks)
        }

        _tugasGrouped.postValue(filtered)
    }

    // ================================
    // KATEGORI WAKTU (FIX: lebih akurat)
    // ================================
    private fun getKategoriWaktu(deadline: String): String {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val todayCal = Calendar.getInstance()
            val todayStr = sdf.format(todayCal.time)

            val tomorrowCal = Calendar.getInstance()
            tomorrowCal.add(Calendar.DATE, 1)
            val tomorrowStr = sdf.format(tomorrowCal.time)

            val deadlineDate = sdf.parse(deadline)
            val todayDate = sdf.parse(todayStr)

            when {
                deadline == todayStr -> "Hari Ini"
                deadline == tomorrowStr -> "Besok"
                deadlineDate != null && todayDate != null && deadlineDate.before(todayDate) -> "Terlambat"
                else -> "Mendatang"
            }

        } catch (e: Exception) {
            "Mendatang"
        }
    }

    // ================================
    // GROUP BY WAKTU (MAIN)
    // ================================
    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val allTasks = snapshot.children.flatMap { villaSnap ->
                        villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }
                    }

                    val grouped = allTasks.groupBy {
                        getKategoriWaktu(it.deadline)
                    }

                    val sortedGroups = grouped.map { (kategori, tasks) ->
                        VillaTugasGroup(
                            villa_id = kategori,
                            namaVilla = kategori, // 🔥 ini jadi header waktu
                            listTugas = tasks
                        )
                    }.sortedBy {
                        when (it.namaVilla) {
                            "Hari Ini" -> 1
                            "Besok" -> 2
                            "Terlambat" -> 3
                            else -> 4
                        }
                    }

                    rawGroups = sortedGroups
                    _rawGroupsLive.postValue(sortedGroups)
                    _tugasGrouped.postValue(sortedGroups)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================================
    // PROGRESS PER VILLA (SUDAH BAGUS, DISEDERHANAKAN)
    // ================================
    fun getTugasGroupedByVillaMurni() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val groups = snapshot.children.mapNotNull { villaSnap ->

                        val tasks = villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }

                        if (tasks.isEmpty()) return@mapNotNull null

                        val total = tasks.size
                        val selesai = tasks.count {
                            it.status.equals("selesai", true)
                        }

                        val persen = if (total > 0) {
                            "${(selesai * 100) / total}%"
                        } else "0%"

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

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================================
    // MASTER DATA
    // ================================
    fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _staffList.postValue(
                        snapshot.children.mapNotNull {
                            it.getValue(UserModel::class.java)
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getVillaList() {
        db.child(FirebaseConfig.PATH_VILLAS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _villaList.postValue(
                        snapshot.children.mapNotNull {
                            it.getValue(VillaModel::class.java)?.apply {
                                id = it.key ?: ""
                            }
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================================
    // AUTO UPDATE SUMMARY
    // ================================
    private fun updateVillaSummary(villaId: String) {
        val path = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")

        path.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val total = snapshot.childrenCount.toInt()
                val completed = snapshot.children.count {
                    it.child("status").value.toString().equals("selesai", true)
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

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ================================
    // CRUD
    // ================================
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
            if (it.isSuccessful) updateVillaSummary(villaId)
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