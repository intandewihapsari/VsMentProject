package com.indri.vsmentproject.ViewHolder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.Data.AnalisisCepatModel
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding

class AnalisisCepatViewHolder (
    private val binding: ItemAnalisisCepatBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: List<AnalisisCepatModel>) {
        binding.jmlLaporan.text = "6"
        binding.jmlBarangRusak.text = "7"
    }
}