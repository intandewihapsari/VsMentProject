package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(private val binding: ItemNotifikasiUrgentBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(list: List<NotifikasiModel>) {
        if (list.isNotEmpty()) {
            val notif = list[0]
            binding.tvTitle.text = notif.judul ?: "Tidak ada judul"
            binding.tvMessage.text = notif.pesan ?: "Tidak ada pesan"
        }
    }
}