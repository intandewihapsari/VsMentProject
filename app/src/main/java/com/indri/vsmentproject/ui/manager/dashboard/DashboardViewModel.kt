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

    // Gunakan AnalisisCepatModel sesuai yang kamu buat
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
                        if (status.equals("selesai", ignoreCase = true)) {
                            totalSelesai++
                        }
                    }
                }

                val progressPercent = if (totalSeluruhTugas > 0) (totalSelesai * 100 / totalSeluruhTugas) else 0

                // PERBAIKAN: Gunakan model baru dan hapus kurung tutup double
                val hasilAnalisis = AnalisisCepatModel(
                    progressTugas = "$progressPercent%",
                    jumlahLaporan = 0, // Nanti bisa ditambah logic hitung laporan
                    barangRusak = 0    // Nanti bisa ditambah logic hitung barang rusak
                )
                _analisisNyata.postValue(hasilAnalisis)
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
        val pendingTaskSource = taskRepo.getAllPendingTasks()

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = _analisisNyata.value
            val inventarisRes = inventarisSource.value
            val taskRes = pendingTaskSource.value

            if (notifRes !is Resource.Loading && inventarisRes !is Resource.Loading) {
                val items = mutableListOf<DashboardItem>()

                notifRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

                // Kirim hasil hitungan ke DashboardItem
                analisisData?.let { items.add(DashboardItem.AnalisisCepat(it)) }

                items.add(DashboardItem.AksiCepat)
                inventarisRes?.data?.let { items.add(DashboardItem.Inventaris(it)) }
                taskRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.TugasPending(it)) }

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