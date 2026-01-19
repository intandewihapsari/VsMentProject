package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding

class AnalisisCepatViewHolder(private val binding: ItemAnalisisCepatBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<AnalisisCepatModel>) {
        if (list.isNotEmpty()) {
            val data = list[0]

            // Set judul dan keterangan dari model
            binding.tvTitleAnalisis.text = data.judul
            binding.jmlLaporan.text = data.nilai.toString() // Konversi Int ke String
            binding.jmlBarangRusak.text = data.keterangan

            // Logic Persentase Sempurna
            // Karena 'nilai' sudah Int, kita tidak perlu toIntOrNull() lagi
            val totalTugas = data.nilai

            // Misal: keterangan berisi angka dalam bentuk String atau kita ambil dari data lain
            val jumlahRusak = data.keterangan.toIntOrNull() ?: 0

            // Perhitungan progres aman
            val persen = if (totalTugas > 0) (jumlahRusak * 100 / totalTugas) else 0

            binding.progressBar.progress = persen
            binding.tvPersentase.text = "$persen%"
        }
    }
}