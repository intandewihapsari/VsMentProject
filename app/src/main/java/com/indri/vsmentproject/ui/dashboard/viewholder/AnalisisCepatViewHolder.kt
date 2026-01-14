package com.indri.vsmentproject.ui.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding

class AnalisisCepatViewHolder(private val binding: ItemAnalisisCepatBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<AnalisisCepatModel>) {
        if (list.isNotEmpty()) {
            val data = list[0]

            // Set angka ke TextView sesuai ID XML kamu
            binding.jmlLaporan.text = data.nilai       // Total Tugas
            binding.jmlBarangRusak.text = data.keterangan // Total Rusak

            // Bonus: Gerakin Progress Bar (Contoh: (Rusak/Total) * 100)
            val total = data.nilai.toIntOrNull() ?: 1
            val rusak = data.keterangan.toIntOrNull() ?: 0
            val persen = if (total > 0) (rusak * 100 / total) else 0

            // Jika di XML ada ID progressBar, aktifkan ini:
            // binding.progressBar.progress = persen
        }
    }
}