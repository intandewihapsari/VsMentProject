package com.indri.vsmentproject.ui.manager.dashboard

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.repository.*
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.data.utils.FirebaseConfig

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val staffRepo = StaffRepository()
    private val notifRepo = NotificationRepository()
    private val db = FirebaseDatabase.getInstance().reference

    private val _managerUid = MutableLiveData<String>()
    private val _analisisNyata = MutableLiveData<AnalisisCepatModel>()

    private fun hitungAnalisisRealtime() {
        val pathTugas = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
        pathTugas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSeluruhTugas = 0
                var totalSelesai = 0

                snapshot.children.forEach { villaSnap ->
                    val listTugas = villaSnap.child("list_tugas")
                    totalSeluruhTugas += listTugas.childrenCount.toInt()
                    listTugas.children.forEach { tugas ->
                        val status = tugas.child("status").value.toString()
                        if (status.equals("selesai", ignoreCase = true)) totalSelesai++
                    }
                }

                val progressPercent = if (totalSeluruhTugas > 0) (totalSelesai * 100 / totalSeluruhTugas) else 0
                _analisisNyata.postValue(AnalisisCepatModel(
                    progressTugas = "$progressPercent%",
                    jumlahLaporan = 0,
                    barangRusak = 0
                ))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        hitungAnalisisRealtime()
        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        val notifSource = notifRepo.getMyNotifications(uid)
        val inventarisSource = taskRepo.getInventarisSummary()
        val pendingTaskSource = taskRepo.getAllPendingTasks() // Pastikan ini LiveData aktif

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = _analisisNyata.value
            val inventarisRes = inventarisSource.value
            val taskRes = pendingTaskSource.value

            // Dashboard tetap bisa tampil meski tugas pending kosong
            if (notifRes !is Resource.Loading && inventarisRes !is Resource.Loading) {
                val items = mutableListOf<DashboardItem>()

                // 1. Notifikasi Urgent
                notifRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

                // 2. Analisis Cepat
                analisisData?.let { items.add(DashboardItem.AnalisisCepat(it)) }

                // 3. Aksi Cepat
                items.add(DashboardItem.AksiCepat)

                // 4. Inventaris
                inventarisRes?.data?.let { items.add(DashboardItem.Inventaris(it)) }

                // 5. Tugas Pending (AMBIL TOP 5 TERBARU)
                taskRes?.data?.let { list ->
                    if (list.isNotEmpty()) {
                        // Urutkan berdasarkan created_at terbaru lalu ambil 5
                        val top5 = list.sortedByDescending { it.created_at }.take(5)
                        items.add(DashboardItem.TugasPending(top5))
                    }
                }

                mediator.value = Resource.Success(items)
            }
        }

        mediator.addSource(notifSource) { updateCombinedResult() }
        mediator.addSource(_analisisNyata) { updateCombinedResult() }
        mediator.addSource(inventarisSource) { updateCombinedResult() }
        mediator.addSource(pendingTaskSource) { updateCombinedResult() }

        mediator
    }

    fun setManagerUid(uid: String) {
        _managerUid.value = uid
    }
}