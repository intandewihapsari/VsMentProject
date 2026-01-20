package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding

class AnalisisCepatViewHolder(
    private val binding: ItemAnalisisCepatBinding,
    private val onReloadClick: () -> Unit // Tambahkan callback untuk reload
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AnalisisCepatModel) {
        // Tampilkan Data ke UI
        binding.tvPersentase.text = "${item.progressTugas} Tugas"
        binding.jmlLaporan.text = item.jumlahLaporan.toString()
        binding.jmlBarangRusak.text = item.barangRusak.toString()

        // Logika Circular Progress
        val progressInt = item.progressTugas.replace("%", "").toIntOrNull() ?: 0
        binding.progressBar.progress = progressInt

        // AKTIFKAN TOMBOL RELOAD
        binding.btnRefresh.setOnClickListener {
            onReloadClick()
        }
    }
}