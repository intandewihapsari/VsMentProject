package com.indri.vsmentproject.ui.manager.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.databinding.ItemAnalisisCepatBinding

class AnalisisCepatViewHolder(
    private val binding: ItemAnalisisCepatBinding,
    private val onReloadClick: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AnalisisCepatModel) {
        binding.tvPersentase.text = item.progressTugas
        binding.tvPersentasePanjang.text = "${item.progressTugas} tugas telah diselesaikan"

        val progressInt = item.progressTugas.replace("%", "").toIntOrNull() ?: 0
        binding.progressBar.progress = progressInt

        binding.btnRefresh.setOnClickListener {
            onReloadClick()
        }
    }
}