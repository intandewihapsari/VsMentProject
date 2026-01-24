package com.indri.vsmentproject.ui.staff.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemVillaParentBinding

class VillaTugasAdapter(
    private val onDoneClick: (TugasModel) -> Unit,
    private val onReportClick: (TugasModel) -> Unit
) : RecyclerView.Adapter<VillaTugasAdapter.VillaViewHolder>() {

    private var items = mutableListOf<VillaTugasGroup>()

    fun setData(newList: List<VillaTugasGroup>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VillaViewHolder {
        val binding = ItemVillaParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VillaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VillaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VillaViewHolder(val binding: ItemVillaParentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: VillaTugasGroup) {
            binding.tvNamaVilla.text = data.namaVilla
            binding.tvProgress.text = "${data.tugasSelesai} / ${data.totalTugas} Tugas Selesai"

            // Toggle Expand/Collapse
            binding.rvTugasChild.visibility = if (data.isExpanded) View.VISIBLE else View.GONE
            binding.ivChevron.rotation = if (data.isExpanded) 180f else 0f

            binding.root.setOnClickListener {
                data.isExpanded = !data.isExpanded
                notifyItemChanged(adapterPosition)
            }

            // Setup Child Adapter (List Tugas)
            val childAdapter = TugasChildAdapter(data.listTugas, onDoneClick, onReportClick)
            binding.rvTugasChild.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = childAdapter
            }
        }
    }
}