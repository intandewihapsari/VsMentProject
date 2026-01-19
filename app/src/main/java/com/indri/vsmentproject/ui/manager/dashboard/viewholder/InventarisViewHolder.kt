package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.databinding.ItemInventarisBinding

class InventarisViewHolder(private val binding: ItemInventarisBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: InventarisModel) {
        // Menggunakan nama field yang BENAR sesuai InventarisModel kamu
        binding.tvPerluGanti.text = data.total_rusak.toString()
        binding.tvPerluPeriksa.text = data.total_perlu_cek.toString()
        binding.tvLayakPakai.text = data.total_aman.toString()
    }
}