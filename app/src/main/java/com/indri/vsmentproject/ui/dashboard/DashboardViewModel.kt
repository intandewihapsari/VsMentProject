package com.indri.vsmentproject.ui.dashboard

import androidx.lifecycle.*
import com.indri.vsmentproject.data.repository.StaffRepository
import com.indri.vsmentproject.data.repository.TaskRepository
import com.indri.vsmentproject.data.repository.AuthRepository

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val notifRepo = StaffRepository()
    private val userRepo = AuthRepository()

    val urgentNotifications = notifRepo.getUrgentNotifications()
    val analisisCepatData = notifRepo.getAnalisisCepat()
    val inventarisData = taskRepo.getInventarisSummary()
    val pendingTasks = taskRepo.getAllPendingTasks()

    val combinedDashboardData = MediatorLiveData<List<DashboardItem>>().apply {
        addSource(urgentNotifications) { value = buildDashboardList() }
        addSource(analisisCepatData) { value = buildDashboardList() }
        addSource(inventarisData) { value = buildDashboardList() }
        addSource(pendingTasks) { value = buildDashboardList() }
    }

    private fun buildDashboardList(): List<DashboardItem> {
        val items = mutableListOf<DashboardItem>()
        urgentNotifications.value?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

        // Analisis Cepat sekarang PASTI MASUK karena kita hitung dari Task
        analisisCepatData.value?.let { items.add(DashboardItem.AnalisisCepat(it)) }

        items.add(DashboardItem.AksiCepat)
        inventarisData.value?.let { items.add(DashboardItem.Inventaris(it)) }
        pendingTasks.value?.let { if (it.isNotEmpty()) items.add(DashboardItem.TugasPending(it)) }
        return items
    }
}