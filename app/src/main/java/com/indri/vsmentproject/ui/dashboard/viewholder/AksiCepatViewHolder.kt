package com.indri.vsmentproject.ui.dashboard.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemAksiCepatBinding

class AksiCepatViewHolder(
    val binding: ItemAksiCepatBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(onTambahTugas: () -> Unit, onKirimNotif: () -> Unit) {
        binding.btnTambahTugas.setOnClickListener {
            onTambahTugas()
        }
        binding.btnKirimNotifikasi.setOnClickListener {
            onKirimNotif()
        }
    }
}