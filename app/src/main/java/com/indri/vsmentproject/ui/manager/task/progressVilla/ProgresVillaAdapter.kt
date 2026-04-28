package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding

class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = mutableListOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProgresVillaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: ItemProgresVillaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: VillaTugasGroup) {

            binding.tvNamaVilla.text = group.namaVilla

            val total = group.listTugas.size
            val selesai = group.listTugas.count { it.status == "selesai" }
            val pending = total - selesai

            binding.tvSelesaiCount.text = "Selesai : $selesai"
            binding.tvPendingCount.text = "Pending : $pending"

            val progress = if (total > 0) (selesai * 100) / total else 0
            binding.tvPersen.text = "$progress%"
            binding.pbProgres.progress = progress

            // 🔥 PENTING: pakai state dari data
            binding.layoutDetail.visibility =
                if (group.isExpanded) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                group.isExpanded = !group.isExpanded
                notifyItemChanged(adapterPosition)
            }

            // 🔥 set adapter tugas
            val tugasAdapter = TugasExpandableAdapter(group.listTugas)

            binding.rvTugas.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = tugasAdapter
            }
        }
    }
}