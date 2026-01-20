package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.databinding.ItemInventarisBinding

class InventarisViewHolder(private val binding: ItemInventarisBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: InventarisModel) {
        // Pastikan ID ini sesuai dengan android:id di XML terbaru kita
        binding.tvPerluGantiValue.text = data.total_rusak.toString()
        binding.tvPerluPeriksaValue.text = data.total_perlu_cek.toString()
        binding.tvLayakPakaiValue.text = data.total_aman.toString()
    }
}