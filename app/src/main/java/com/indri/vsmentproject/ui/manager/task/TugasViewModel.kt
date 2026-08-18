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
    private val activeListeners = mutableMapOf<Query, ValueEventListener>()

    private var allRawTasks = listOf<TugasModel>()

    private val _waktuListLive = MutableLiveData<List<WaktuContainer>>()
    val waktuListLive: LiveData<List<WaktuContainer>> = _waktuListLive

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _rawGroupsLive = MutableLiveData<List<VillaTugasGroup>>()
    val rawGroupsLive: LiveData<List<VillaTugasGroup>> = _rawGroupsLive

    // --- DATA FETCHING (Operational Path) ---

    fun getTugasGroupedByVilla(managerId: String) {
        val ref = FirebaseConfig.getTaskManagementRef(managerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<TugasModel>()
                // Structure: task_management -> [villaId] -> list_tugas -> [taskId]
                snapshot.children.forEach { villaSnap ->
                    villaSnap.child("list_tugas").children.forEach { tugasSnap ->
                        tugasSnap.getValue(TugasModel::class.java)?.let {
                            it.id = tugasSnap.key ?: ""
                            it.villa_id = villaSnap.key ?: ""
                            
                            // LOG: Verifikasi bukti foto terbaca dari Firebase
                            android.util.Log.d("FIREBASE_FETCH", "Tugas: ${it.tugas} | Bukti Foto Count: ${it.bukti_foto?.size ?: 0}")
                            
                            tasks.add(it)
                        }
                    }
                }
                allRawTasks = tasks
                updateStats(tasks)
                processTasksToWaktuContainer(tasks)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    private fun processTasksToWaktuContainer(tasks: List<TugasModel>) {
        val byWaktu = tasks.groupBy { getKategoriWaktu(it.deadline) }
        val result = mutableListOf<WaktuContainer>()
        val urutan = listOf("Terlambat", "Hari Ini", "Mendatang")

        urutan.forEach { kategori ->
            byWaktu[kategori]?.let { tasksInWaktu ->
                val villaGroups = tasksInWaktu.groupBy { it.villa_id }.map { entry ->
                    val listTugasVilla = entry.value
                    VillaTugasGroup(
                        villa_id = entry.key,
                        namaVilla = listTugasVilla.firstOrNull()?.villa_nama ?: "Villa",
                        listTugas = listTugasVilla,
                        totalTugas = listTugasVilla.size,
                        tugasSelesai = listTugasVilla.count { it.status.equals(FirebaseConfig.STATUS_DONE, true) }
                    )
                }
                result.add(WaktuContainer(kategori, villaGroups))
            }
        }
        _waktuListLive.postValue(result)
    }

    private fun updateStats(tasks: List<TugasModel>) {
        val groups = tasks.groupBy { it.villa_id }.map { entry ->
            val list = entry.value
            VillaTugasGroup(
                villa_id = entry.key,
                namaVilla = list.firstOrNull()?.villa_nama ?: "Unknown Villa",
                listTugas = list,
                totalTugas = list.size,
                tugasSelesai = list.count { it.status.equals(FirebaseConfig.STATUS_DONE, true) }
            )
        }
        _rawGroupsLive.postValue(groups)
    }

    fun filterTugas(status: String) {
        val filtered = if (status == "All") allRawTasks else allRawTasks.filter { it.status.equals(status, true) }
        processTasksToWaktuContainer(filtered)
    }

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

    // --- MASTER DATA (Master Data Path) ---

    fun getStaffList(managerId: String) {
        val ref = FirebaseConfig.getStaffsRef(managerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _staffList.postValue(snapshot.children.mapNotNull { it.getValue(UserModel::class.java) })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    fun getVillaList(managerId: String) {
        val ref = FirebaseConfig.getVillasRef(managerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _villaList.postValue(snapshot.children.mapNotNull {
                    it.getValue(VillaModel::class.java)?.apply { id = it.key ?: "" }
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    override fun onCleared() {
        super.onCleared()
        activeListeners.forEach { (query, listener) ->
            query.removeEventListener(listener)
        }
        activeListeners.clear()
    }

    // --- CRUD (Operational Path) ---

    fun simpanTugasLengkap(managerId: String, villaId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val ref = FirebaseConfig.getTaskManagementRef(managerId).child(villaId).child("list_tugas").push()
        val finalData = data.toMutableMap().apply { put("id", ref.key ?: "") }
        ref.setValue(finalData).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun updateTugas(managerId: String, villaId: String, taskId: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        FirebaseConfig.getTaskManagementRef(managerId).child(villaId).child("list_tugas").child(taskId)
            .updateChildren(data).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun hapusTugas(managerId: String, villaId: String, taskId: String, onComplete: (Boolean) -> Unit) {
        FirebaseConfig.getTaskManagementRef(managerId).child(villaId).child("list_tugas").child(taskId)
            .removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}