package com.indri.vsmentproject.ViewHolder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemInventarisBinding

class InventarisViewHolder (
    private val binding: ItemInventarisBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        binding.tvTitle.text = "Notifikasi Urgent"
        binding.tvMessage.text = "Gas LPG habis di dapur"
    }
}