package com.indri.vsmentproject.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.*
import com.indri.vsmentproject.ui.dashboard.viewholder.*

class DashboardAdapter(
    private var items: List<DashboardItem> = emptyList(),
    private val onTambahTugasClick: () -> Unit,
    private val onKirimNotifClick: () -> Unit,
    private val onTugasClick: (TugasModel) -> Unit // TAMBAHKAN INI SUPAYA TIDAK ERROR
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateData(newItems: List<DashboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is DashboardItem.NotifikasiUrgent -> 0
        is DashboardItem.AnalisisCepat -> 1
        is DashboardItem.AksiCepat -> 2
        is DashboardItem.Inventaris -> 3
        is DashboardItem.TugasPending -> 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> NotifikasiUrgentViewHolder(ItemNotifikasiUrgentBinding.inflate(inflater, parent, false))
            1 -> AnalisisCepatViewHolder(ItemAnalisisCepatBinding.inflate(inflater, parent, false))
            2 -> AksiCepatViewHolder(ItemAksiCepatBinding.inflate(inflater, parent, false))
            3 -> InventarisViewHolder(ItemInventarisBinding.inflate(inflater, parent, false))
            4 -> TugasPendingViewHolder(ItemTugasPendingBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DashboardItem.NotifikasiUrgent -> (holder as NotifikasiUrgentViewHolder).bind(item.data)
            is DashboardItem.AnalisisCepat -> (holder as AnalisisCepatViewHolder).bind(item.data)
            is DashboardItem.AksiCepat -> (holder as AksiCepatViewHolder).bind(onTambahTugasClick, onKirimNotifClick)
            is DashboardItem.Inventaris -> (holder as InventarisViewHolder).bind(item.data)
            is DashboardItem.TugasPending -> (holder as TugasPendingViewHolder).bind(item.listTugas, onTugasClick)
        }
    }

    override fun getItemCount(): Int = items.size
}