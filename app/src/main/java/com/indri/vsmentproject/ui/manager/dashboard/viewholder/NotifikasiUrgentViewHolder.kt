package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(private val binding: ItemNotifikasiUrgentBinding) : RecyclerView.ViewHolder(binding.root) {

    // Perbaiki parameter agar menerima satu objek model, bukan List (agar lebih efisien)
    fun bind(notif: NotifikasiModel) {
        // Sesuaikan ID dengan XML yang Anda kirim (tvJudulJadwal dan tvTime)
        binding.tvJudulJadwal.text = notif.judul ?: "Pemberitahuan Baru"
        binding.tvTime.text = notif.pesan ?: "Cek detail kegiatan penting Anda"
    }
}