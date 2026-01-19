package com.indri.vsmentproject.ui.manager.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.*
import com.indri.vsmentproject.ui.manager.viewholder.AksiCepatViewHolder
import com.indri.vsmentproject.ui.manager.viewholder.AnalisisCepatViewHolder
import com.indri.vsmentproject.ui.manager.viewholder.InventarisViewHolder
import com.indri.vsmentproject.ui.manager.viewholder.NotifikasiUrgentViewHolder
import com.indri.vsmentproject.ui.manager.viewholder.TugasPendingViewHolder

class DashboardAdapter(
    private var items: List<DashboardItem> = emptyList(),
    private val onTambahTugasClick: () -> Unit,
    private val onKirimNotifClick: () -> Unit,
    private val onTugasClick: (TugasModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Fungsi untuk memperbarui data dari Fragment
    fun updateData(newItems: List<DashboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Menentukan jenis layout berdasarkan tipe DashboardItem
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
            0 -> NotifikasiUrgentViewHolder(
                ItemNotifikasiUrgentBinding.inflate(inflater, parent, false)
            )
            1 -> AnalisisCepatViewHolder(
                ItemAnalisisCepatBinding.inflate(inflater, parent, false)
            )
            2 -> AksiCepatViewHolder(
                ItemAksiCepatBinding.inflate(inflater, parent, false)
            )
            3 -> InventarisViewHolder(
                ItemInventarisBinding.inflate(inflater, parent, false)
            )
            4 -> TugasPendingViewHolder(
                ItemTugasPendingBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Tipe View Tidak Dikenal")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Proses "Binding" atau pengisian data ke tiap-tiap ViewHolder
        when (val item = items[position]) {
            is DashboardItem.NotifikasiUrgent -> {
                (holder as NotifikasiUrgentViewHolder).bind(item.data)
            }
            is DashboardItem.AnalisisCepat -> {
                (holder as AnalisisCepatViewHolder).bind(item.data)
            }
            is DashboardItem.AksiCepat -> {
                // Aksi Cepat butuh lambda untuk handle klik tombol di Dashboard
                (holder as AksiCepatViewHolder).bind(onTambahTugasClick, onKirimNotifClick)
            }
            is DashboardItem.Inventaris -> {
                (holder as InventarisViewHolder).bind(item.data)
            }
            is DashboardItem.TugasPending -> {
                // Tugas Pending butuh list data dan aksi saat item tugas diklik
                (holder as TugasPendingViewHolder).bind(item.listTugas, onTugasClick)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}