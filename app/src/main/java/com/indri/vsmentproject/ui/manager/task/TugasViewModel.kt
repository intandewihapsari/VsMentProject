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

    // Simpan data murni dari Firebase untuk keperluan filter & statistik
    private var allRawTasks = listOf<TugasModel>()

    // LiveData utama untuk UI Card (Waktu > Villa > Tugas)
    private val _waktuListLive = MutableLiveData<List<WaktuContainer>>()
    val waktuListLive: LiveData<List<WaktuContainer>> = _waktuListLive

    // LiveData untuk Master Data
    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    // LiveData lama tetap dipertahankan untuk statistik persentase di Fragment
    private val _rawGroupsLive = MutableLiveData<List<VillaTugasGroup>>()
    val rawGroupsLive: LiveData<List<VillaTugasGroup>> = _rawGroupsLive

    // --- DATA FETCHING ---

    fun getTugasGroupedByVilla() {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = snapshot.children.flatMap { villaSnap ->
                        villaSnap.child("list_tugas").children.mapNotNull {
                            it.getValue(TugasModel::class.java)?.apply { id = it.key ?: "" }
                        }
                    }
                    allRawTasks = tasks // Simpan data asli

                    // Update statistik (untuk rawGroupsLive di fragment)
                    updateStats(tasks)

                    // Proses data ke UI Card
                    processTasksToWaktuContainer(tasks)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun processTasksToWaktuContainer(tasks: List<TugasModel>) {
        val byWaktu = tasks.groupBy { getKategoriWaktu(it.deadline) }
        val result = mutableListOf<WaktuContainer>()
        val urutan = listOf("Terlambat", "Hari Ini", "Besok", "Mendatang")

        urutan.forEach { kategori ->
            byWaktu[kategori]?.let { tasksInWaktu ->
                // Perbaikan di sini: Mapping ke VillaTugasGroup bukan VillaGroup
                val villaGroups = tasksInWaktu.groupBy { it.villa_id }.map { entry ->
                    val listTugasVilla = entry.value
                    val total = listTugasVilla.size
                    val selesai = listTugasVilla.count { it.status.equals("selesai", true) }

                    VillaTugasGroup(
                        villa_id = entry.key,
                        namaVilla = listTugasVilla.firstOrNull()?.villa_nama ?: "Villa",
                        listTugas = listTugasVilla,
                        totalTugas = total,
                        tugasSelesai = selesai,
                        isExpanded = false // Default tertutup sesuai mau kamu
                    )
                }
                result.add(WaktuContainer(kategori, villaGroups))
            }
        }
        _waktuListLive.postValue(result)
    }

    private fun updateStats(tasks: List<TugasModel>) {
        // Kelompokkan semua tugas berdasarkan villa_id untuk statistik
        val groups = tasks.groupBy { it.villa_id }.map { entry ->
            val list = entry.value
            val total = list.size
            val selesai = list.count { it.status.equals("selesai", true) || it.status.equals("done", true) }

            VillaTugasGroup(
                villa_id = entry.key,
                namaVilla = list.firstOrNull()?.villa_nama ?: "Unknown Villa",
                listTugas = list, // List tugas tetap dibawa untuk hitung count di adapter
                totalTugas = total,
                tugasSelesai = selesai
            )
        }
        // Kirim ke LiveData yang akan di-observe oleh ProgresDetailFragment
        _rawGroupsLive.postValue(groups)
    }
    // --- FILTER ---

    fun filterTugas(status: String) {
        val filtered = if (status == "All") {
            allRawTasks
        } else {
            allRawTasks.filter { it.status.equals(status, true) }
        }
        processTasksToWaktuContainer(filtered)
    }

    // --- HELPER ---

    private fun getKategoriWaktu(deadline: String): String {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val taskDate = Calendar.getInstance().apply { time = sdf.parse(deadline)!! }

            when {
                taskDate.before(today) -> "Terlambat"
                sdf.format(taskDate.time) == sdf.format(today.time) -> "Hari Ini"
                else -> "Mendatang"
            }
        } catch (e: Exception) { "Mendatang" }
    }



    // --- MASTER DATA & CRUD ---

    fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _staffList.postValue(snapshot.children.mapNotNull { it.getValue(UserModel::class.java) })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getVillaList() {
        db.child(FirebaseConfig.PATH_VILLAS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _villaList.postValue(snapshot.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun simpanTugasLengkap(villaId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val ref = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").push()
        val finalData = data.toMutableMap().apply { put("id", ref.key ?: "") }
        ref.setValue(finalData).addOnCompleteListener { onComplete(it.isSuccessful) }
    }


    fun updateTugas(villaId: String, taskId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .updateChildren(data).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun hapusTugas(villaId: String, taskId: String, onComplete: (Boolean) -> Unit) {
        db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas").child(taskId)
            .removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}