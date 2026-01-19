package com.indri.vsmentproject.ui.manager.dashboard

import androidx.lifecycle.*
import com.indri.vsmentproject.data.repository.*
import com.indri.vsmentproject.data.utils.Resource

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val staffRepo = StaffRepository()
    private val notifRepo = NotificationRepository()

    private val _managerUid = MutableLiveData<String>()

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        // Sesuai dengan nama fungsi di Repo kamu:
        val notifSource = notifRepo.getMyNotifications(uid) // Mengembalikan Resource
        val analisisSource = staffRepo.getAnalisisCepat() // Mengembalikan List
        val inventarisSource = taskRepo.getInventarisSummary() // Mengembalikan Resource
        val pendingTaskSource = taskRepo.getAllPendingTasks() // Mengembalikan Resource

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = analisisSource.value // Langsung List, bukan Resource
            val inventarisRes = inventarisSource.value
            val taskRes = pendingTaskSource.value

            // Cek Loading hanya pada yang menggunakan wrapper Resource
            if (notifRes !is Resource.Loading &&
                inventarisRes !is Resource.Loading &&
                taskRes !is Resource.Loading) {

                val items = mutableListOf<DashboardItem>()

                // 1. Notifikasi (Urutkan reversed sesuai Repo kamu)
                notifRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

                // 2. Analisis Cepat (Karena Repo kirim List langsung)
                analisisData?.let { if (it.isNotEmpty()) items.add(DashboardItem.AnalisisCepat(it)) }

                // 3. Tombol Aksi (Static)
                items.add(DashboardItem.AksiCepat)

                // 4. Inventaris Summary
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