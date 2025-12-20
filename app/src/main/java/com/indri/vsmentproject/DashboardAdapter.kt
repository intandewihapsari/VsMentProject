package com.indri.vsmentproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.ViewHolder.AnalisisCepatViewHolder
import com.indri.vsmentproject.ViewHolder.NotifikasiUrgentViewHolder
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class DashboardAdapter(
    private var items: List<DashboardItem> = emptyList()
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_NOTIFIKASI -> {
                val binding = ItemNotifikasiUrgentBinding
                    .inflate(inflater, parent, false)
                NotifikasiUrgentViewHolder(binding)
            }
            TYPE_ANALISIS -> {
                val binding = ItemAnalisisCepatBinding
                    .inflate(inflater, parent, false)
                AnalisisCepatViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Belum dipakai")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = items[position]) {
            is DashboardItem.NotifikasiUrgent -> {
                (holder as NotifikasiUrgentViewHolder).bind(item.data)
            }
            is DashboardItem.AnalisisCepat -> {
                (holder as AnalisisCepatViewHolder).bind(item.data)
            }
            else -> Unit
        }
    }
}
