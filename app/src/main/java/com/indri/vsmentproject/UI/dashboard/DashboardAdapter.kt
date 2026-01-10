package com.indri.vsmentproject.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.UI.dashboard.DashboardItem
import com.indri.vsmentproject.UI.dashboard.viewholder.AksiCepatViewHolder
import com.indri.vsmentproject.UI.dashboard.viewholder.AnalisisCepatViewHolder
import com.indri.vsmentproject.UI.dashboard.viewholder.InventarisViewHolder
import com.indri.vsmentproject.UI.dashboard.viewholder.NotifikasiUrgentViewHolder
import com.indri.vsmentproject.UI.dashboard.viewholder.TugasPendingViewHolder
import com.indri.vsmentproject.databinding.ItemAksiCepatBinding
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding
import com.indri.vsmentproject.databinding.ItemInventarisBinding
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding

class DashboardAdapter(
    private var items: List<DashboardItem> = emptyList(),
    private val onTambahTugasClick: () -> Unit,
    private val onKirimNotifClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(newItems: List<DashboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    companion object {
        private const val TYPE_NOTIFIKASI = 0
        private const val TYPE_ANALISIS = 1
        private const val TYPE_AKSI = 2
        private const val TYPE_INVENTARIS = 3
        private const val TYPE_TUGAS = 4
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is DashboardItem.NotifikasiUrgent -> TYPE_NOTIFIKASI
            is DashboardItem.AnalisisCepat -> TYPE_ANALISIS
            is DashboardItem.AksiCepat -> TYPE_AKSI
            is DashboardItem.Inventaris -> TYPE_INVENTARIS
            is DashboardItem.TugasPending -> TYPE_TUGAS
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NOTIFIKASI -> NotifikasiUrgentViewHolder(
                ItemNotifikasiUrgentBinding.inflate(inflater, parent, false)
            )
            TYPE_ANALISIS -> AnalisisCepatViewHolder(
                ItemAnalisisCepatBinding.inflate(inflater, parent, false)
            )
            TYPE_AKSI -> AksiCepatViewHolder(
                ItemAksiCepatBinding.inflate(inflater, parent, false)
            )
            // TAMBAHKAN INI AGAR TIDAK ERROR
            TYPE_INVENTARIS -> InventarisViewHolder(
                ItemInventarisBinding.inflate(inflater, parent, false)
            )
            TYPE_TUGAS -> TugasPendingViewHolder(
                ItemTugasPendingBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Tipe view tidak dikenal: $viewType")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DashboardItem.NotifikasiUrgent -> (holder as NotifikasiUrgentViewHolder).bind(item.data)
            is DashboardItem.AnalisisCepat -> (holder as AnalisisCepatViewHolder).bind(item.data)
            is DashboardItem.AksiCepat -> (holder as AksiCepatViewHolder).bind(onTambahTugasClick, onKirimNotifClick)
            is DashboardItem.Inventaris -> {
                (holder as InventarisViewHolder).bind(
                    item.data.perluGanti,
                    item.data.perluPeriksa,
                    item.data.layakPakai
                )
            }
            is DashboardItem.TugasPending -> {
                // Pastikan holder di-cast ke TugasPendingViewHolder
                val tugasHolder = holder as TugasPendingViewHolder
                tugasHolder.bind(item.listTugas)
            }
        }
    }
}