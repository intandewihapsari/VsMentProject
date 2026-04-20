package com.indri.vsmentproject.ui.manager.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemAksiCepatBinding
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding
import com.indri.vsmentproject.databinding.ItemGroupTugasBinding
import com.indri.vsmentproject.databinding.ItemInventarisBinding
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding
// Import viewholder lainnya sesuai lokasi projectmu
import com.indri.vsmentproject.ui.manager.viewholder.*
class DashboardAdapter(
    private val onTambahTugasClick: () -> Unit,
    private val onKirimNotifClick: () -> Unit,
    private val onEditTugas: (TugasModel) -> Unit, // Ini parameter ke-3
    private val onReloadAnalisisClick: () -> Unit  // Ini parameter ke-4
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<DashboardItem>()

    fun updateData(newList: List<DashboardItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DashboardItem.AksiCepat -> 0
            is DashboardItem.AnalisisCepat -> 1
            is DashboardItem.NotifikasiUrgent -> 2
            is DashboardItem.TugasPending -> 3
            is DashboardItem.Inventaris -> 4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> AksiCepatViewHolder(ItemAksiCepatBinding.inflate(inflater, parent, false))

            // Perbaikan viewType 1 untuk Analisis
            1 -> AnalisisCepatViewHolder(
                ItemAnalisisCepatBinding.inflate(inflater, parent, false),
                onReloadAnalisisClick
            )

            // Perbaikan viewType 2 untuk Notifikasi
            2 -> NotifikasiUrgentViewHolder(ItemNotifikasiUrgentBinding.inflate(inflater, parent, false))

            3 -> TugasPendingViewHolder(ItemGroupTugasBinding.inflate(inflater, parent, false))

            else -> InventarisViewHolder(ItemInventarisBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DashboardItem.AksiCepat -> (holder as AksiCepatViewHolder).bind(onTambahTugasClick, onKirimNotifClick)
            is DashboardItem.AnalisisCepat -> (holder as AnalisisCepatViewHolder).bind(item.data)
            is DashboardItem.NotifikasiUrgent -> if (item.data.isNotEmpty()) (holder as NotifikasiUrgentViewHolder).bind(item.data[0])
            is DashboardItem.TugasPending -> (holder as TugasPendingViewHolder).bind(item.dataGroups, onEditTugas)
            is DashboardItem.Inventaris -> (holder as InventarisViewHolder).bind(item.data)
        }
    }

    override fun getItemCount(): Int = items.size
}