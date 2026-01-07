package com.indri.vsmentproject.UI.dashboard

import com.indri.vsmentproject.Data.Model.AnalisisCepatModel
import com.indri.vsmentproject.Data.Model.InventarisModel
import com.indri.vsmentproject.Data.Model.NotifikasiModel

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

    object TugasPending : DashboardItem()
}



