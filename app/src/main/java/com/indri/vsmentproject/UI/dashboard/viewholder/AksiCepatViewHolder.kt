package com.indri.vsmentproject.UI.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemAksiCepatBinding

class AksiCepatViewHolder (
    private val binding: ItemAksiCepatBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        binding.tvTitle.text = "Notifikasi Urgent"
        binding.tvMessage.text = "Gas LPG habis di dapur"
    }
}