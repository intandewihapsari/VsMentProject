package com.indri.vsmentproject.ui.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(private val binding: ItemNotifikasiUrgentBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<NotifikasiModel>) {
        if (list.isNotEmpty()) {
            val notif = list[0]

            // Set data sesuai ID tvTitle dan tvMessage di XML
            binding.tvTitle.text = notif.judul
            binding.tvMessage.text = notif.pesan
        }
    }
}