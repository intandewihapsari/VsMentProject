package com.indri.vsmentproject.ui.manager.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemGroupTugasBinding
import com.indri.vsmentproject.ui.manager.task.TugasItemAdapter

class TugasPendingViewHolder(
    private val binding: ItemGroupTugasBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        groups: List<VillaTugasGroup>, // Ambil data group villa
        onTugasClick: (TugasModel) -> Unit
    ) {
        // 1. Ubah Header menjadi "Tugas Pending"
        binding.tvHeader.text = "Tugas Pending"

        // 2. Ambil hanya 1 tugas (Top 1) dari setiap Villa
        val topTasks = groups.mapNotNull { it.listTugas.firstOrNull() }

        // 3. Setup RecyclerView Inner dengan Adapter (Bukan addView)
        val adapter = TugasItemAdapter(
            onEdit = { onTugasClick(it) },
            onDelete = {} // Dashboard biasanya tidak ada swipe delete
        )

        binding.rvInner.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(itemView.context)
            isNestedScrollingEnabled = false
        }

        adapter.updateList(topTasks)
    }
}