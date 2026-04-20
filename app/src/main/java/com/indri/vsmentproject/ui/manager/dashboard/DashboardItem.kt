package com.indri.vsmentproject.ui.manager.dashboard

import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup

sealed class DashboardItem {
    object AksiCepat : DashboardItem()
    data class NotifikasiUrgent(val data: List<NotifikasiModel>) : DashboardItem()
    data class AnalisisCepat(val data: AnalisisCepatModel) : DashboardItem()
    data class Inventaris(val data: InventarisModel) : DashboardItem()
    data class TugasPending(val dataGroups: List<VillaTugasGroup>) : DashboardItem()}