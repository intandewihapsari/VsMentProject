package com.indri.vsmentproject

import com.indri.vsmentproject.Data.AnalisisCepatModel
import com.indri.vsmentproject.Data.NotifikasiModel

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



