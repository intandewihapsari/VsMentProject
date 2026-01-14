package com.indri.vsmentproject.ui.dashboard

import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.task.InventarisModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.task.TugasModel

sealed class DashboardItem {
    data class NotifikasiUrgent(
        val data: List<NotifikasiModel>
    ) : DashboardItem()

    data class AnalisisCepat(
        val data: List<AnalisisCepatModel>
    ) : DashboardItem()

    object AksiCepat : DashboardItem()
    data class Inventaris(
        val data: InventarisModel
    ) : DashboardItem()

    data class TugasPending(
        val listTugas: List<TugasModel> // Harus List agar bind(item.listTugas) tidak error
    ) : DashboardItem()
}



