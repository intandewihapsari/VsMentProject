package com.indri.vsmentproject.ui.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemInventarisBinding

class InventarisViewHolder(val binding: ItemInventarisBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(ganti: Int, periksa: Int, layak: Int) {
        binding.tvPerluGanti.text = "Perlu Ganti : $ganti"
        binding.tvPerluPeriksa.text = "Perlu Periksa : $periksa"
        binding.tvLayakPakai.text = "Layak Pakai : $layak"
    }
}