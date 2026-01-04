package com.indri.vsmentproject.UI.dashboard.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.Data.Model.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(
    private val binding: ItemNotifikasiUrgentBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: List<NotifikasiModel>) {

        // ambil notifikasi urgent pertama
        val urgent = data.firstOrNull() ?: run {
            binding.root.visibility = View.GONE
            return
        }

        binding.tvTitle.text = urgent.judul
        binding.tvMessage.text = urgent.pesan
    }
}
