package com.indri.vsmentproject.ui.dashboard

import com.indri.vsmentproject.data.model.AnalisisCepatModel
import com.indri.vsmentproject.data.model.InventarisModel
import com.indri.vsmentproject.data.model.notifikasi.NotifikasiModel
import com.indri.vsmentproject.data.model.tugas.TugasModel

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



