package com.indri.vsmentproject.UI.dashboard

import com.indri.vsmentproject.Data.Model.AnalisisCepatModel
import com.indri.vsmentproject.Data.Model.NotifikasiModel

sealed class DashboardItem {
    data class NotifikasiUrgent(
        val data: List<NotifikasiModel>
    ) : DashboardItem()

    data class AnalisisCepat(
        val data: List<AnalisisCepatModel>
    ) : DashboardItem()

    object AksiCepat : DashboardItem()
    object Inventaris : DashboardItem()
    object TugasPending : DashboardItem()
}



