package com.indri.vsmentproject.ui.manager.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemGroupTugasBinding
import com.indri.vsmentproject.ui.manager.task.TugasItemAdapter
import android.view.ViewGroup
import android.widget.LinearLayout

class TugasPendingViewHolder(
    private val binding: ItemGroupTugasBinding

) : RecyclerView.ViewHolder(binding.root) {

    private fun dpToPx(dp: Int): Int {
        val density = itemView.context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
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
            onDelete = {},
            showStatus = false
        )

        binding.rvInner.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(itemView.context)
            isNestedScrollingEnabled = false
        }

        adapter.updateList(topTasks)

        val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        binding.root.layoutParams = params
    }
}