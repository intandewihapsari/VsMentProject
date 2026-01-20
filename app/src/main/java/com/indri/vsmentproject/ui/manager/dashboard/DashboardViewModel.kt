package com.indri.vsmentproject.ui.manager.dashboard

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.repository.*
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.data.utils.FirebaseConfig

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val staffRepo = StaffRepository()
    private val notifRepo = NotificationRepository()
    private val db = FirebaseDatabase.getInstance().reference

    private val _managerUid = MutableLiveData<String>()

    // --- FUNGSI BARU: Paksa Hitung Ulang untuk Dashboard ---
    private fun refreshSummaryBeforeLoading(villaId: String) {
        val pathTugas = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("list_tugas")

        pathTugas.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount.toInt()
                var completed = 0
                snapshot.children.forEach {
                    if (it.child("status").value.toString().equals("selesai", ignoreCase = true)) {
                        completed++
                    }
                }
                val progressPercent = if (total > 0) (completed * 100 / total) else 0

                // Overwrite folder summary yang "50% Palsu" tadi dengan data asli
                val summaryData = mapOf(
                    "total" to total,
                    "completed" to completed,
                    "progress" to "$progressPercent%"
                )
                db.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(villaId).child("summary").setValue(summaryData)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        // SETIAP KALI DASHBOARD DIBUKA, HITUNG ULANG DATA ASLI
        refreshSummaryBeforeLoading("V01")

        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        val notifSource = notifRepo.getMyNotifications(uid)
        val analisisSource = staffRepo.getAnalisisCepat("V01")
        val inventarisSource = taskRepo.getInventarisSummary()
        val pendingTaskSource = taskRepo.getAllPendingTasks()

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = analisisSource.value
            val inventarisRes = inventarisSource.value
            val taskRes = pendingTaskSource.value

            if (notifRes !is Resource.Loading &&
                inventarisRes !is Resource.Loading &&
                taskRes !is Resource.Loading) {

                val items = mutableListOf<DashboardItem>()

                // 1. Notifikasi Urgent
                notifRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

                // 2. Analisis Cepat (Sekarang isinya hasil hitungan asli, bukan 50% lagi)
                analisisData?.let { items.add(DashboardItem.AnalisisCepat(it)) }

                // 3. Tombol Aksi
                items.add(DashboardItem.AksiCepat)

                // 4. Inventaris
                inventarisRes?.data?.let { items.add(DashboardItem.Inventaris(it)) }

                // 5. Tugas Pending
                taskRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.TugasPending(it)) }

                mediator.value = Resource.Success(items)
            }
        }

        mediator.addSource(notifSource) { updateCombinedResult() }
        mediator.addSource(analisisSource) { updateCombinedResult() }
        mediator.addSource(inventarisSource) { updateCombinedResult() }
        mediator.addSource(pendingTaskSource) { updateCombinedResult() }

        mediator
    }

    fun setManagerUid(uid: String) {
        _managerUid.value = uid
    }
}