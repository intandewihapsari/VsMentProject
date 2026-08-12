package com.indri.vsmentproject.ui.manager.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemGroupTugasBinding
import com.indri.vsmentproject.ui.manager.task.TugasItemAdapter

class TugasPendingViewHolder(
    private val binding: ItemGroupTugasBinding
) : RecyclerView.ViewHolder(binding.root) {

    private fun dpToPx(dp: Int): Int {
        val density = itemView.context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun bind(
        groups: List<VillaTugasGroup>,
        onTugasClick: (TugasModel) -> Unit
    ) {
        binding.tvHeader.text = "Tugas Pending"

        // Ambil top 1 tugas pending dari tiap group villa
        val topTasks = groups.mapNotNull { it.listTugas.firstOrNull() }

        val adapter = TugasItemAdapter(
            onEdit = { onTugasClick(it) },
            onDelete = {},
            showStatus = false
        )

        binding.rvInner.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(itemView.context)
            isNestedScrollingEnabled = false
        }

        adapter.updateList(topTasks)

        // PERBAIKAN LOGIKA SAFETY: Proteksi cast MarginLayoutParams agar tidak memicu crash di beberapa versi device layout
        val layoutParams = binding.root.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            binding.root.layoutParams = layoutParams
        }
    }
}