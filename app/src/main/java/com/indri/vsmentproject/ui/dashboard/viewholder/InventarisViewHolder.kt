package com.indri.vsmentproject.ui.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.InventarisModel
import com.indri.vsmentproject.databinding.ItemInventarisBinding

class InventarisViewHolder(private val binding: ItemInventarisBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: InventarisModel) {
        binding.tvPerluGanti.text = data.perluGanti.toString()
        binding.tvPerluPeriksa.text = data.perluPeriksa.toString()
        binding.tvLayakPakai.text = data.layakPakai.toString()
    }
}