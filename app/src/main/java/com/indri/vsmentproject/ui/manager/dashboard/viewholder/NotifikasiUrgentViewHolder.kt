package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(private val binding: ItemNotifikasiUrgentBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notif: NotifikasiModel) {
        // Kalau judul kosong di Firebase, tampilkan "Pemberitahuan"
        binding.tvJudulJadwal.text = if (notif.judul.isNotEmpty()) notif.judul else "Pemberitahuan"

        // Menampilkan pesan
        binding.tvTime.text = notif.pesan

        // Tips: Kalau mau nampilin waktu dari timestamp Long:
        // binding.tvWaktu.text = DateUtils.getRelativeTimeSpanString(notif.timestamp)
    }
}