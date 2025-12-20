package com.indri.vsmentproject.ViewHolder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding

class TugasPendingViewHolder (
        private val binding: ItemTugasPendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvTitle.text = "Notifikasi Urgent"
            binding.tvMessage.text = "Gas LPG habis di dapur"
        }
    }