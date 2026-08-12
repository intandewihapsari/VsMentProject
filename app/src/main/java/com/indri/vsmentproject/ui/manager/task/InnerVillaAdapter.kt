package com.indri.vsmentproject.ui.manager.task

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class InnerVillaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private var originalGroups = listOf<VillaTugasGroup>()
    private val flatItems = mutableListOf<Any>()

    fun setData(villaGroups: List<VillaTugasGroup>) {
        this.originalGroups = villaGroups
        generateFlatItems()
    }

    private fun generateFlatItems() {
        flatItems.clear()
        originalGroups.forEach { group ->
            flatItems.add(group)
            if (group.isExpanded) {
                flatItems.addAll(group.listTugas)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (flatItems[position] is VillaTugasGroup) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(inflater.inflate(R.layout.item_villa_parent, parent, false))
        } else {
            ItemVH(ItemTugasPendingListBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = flatItems[position]
        if (holder is HeaderVH) holder.bind(item as VillaTugasGroup)
        else if (holder is ItemVH) holder.bind(item as TugasModel)
    }

    override fun getItemCount() = flatItems.size

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(group: VillaTugasGroup) {
            val tvNama = itemView.findViewById<TextView>(R.id.tvNamaVilla)
            val tvProgress = itemView.findViewById<TextView>(R.id.tvProgress)
            val ivChevron = itemView.findViewById<ImageView>(R.id.ivChevron)

            tvNama?.text = group.namaVilla
            tvProgress?.text = "${group.tugasSelesai} / ${group.totalTugas} Tugas Selesai"
            ivChevron?.rotation = if (group.isExpanded) 180f else 0f

            itemView.setOnClickListener {
                group.isExpanded = !group.isExpanded
                generateFlatItems()
            }
        }
    }

    inner class ItemVH(val binding: ItemTugasPendingListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tugas: TugasModel) {
            binding.tvVillaName.visibility = View.GONE
            binding.tvNamaTugas.text = "${tugas.tugas} - ${tugas.ruangan}"

            // PERBAIKAN: Gunakan properti staff_nama yang sinkron dengan model data Firebase terupdate
            binding.tvPIC.text = tugas.staff_nama

            val statusData = tugas.status.trim().lowercase()
            binding.tvStatus.text = statusData

            val shape = GradientDrawable().apply {
                cornerRadius = 50f
                when (statusData) {
                    "pending" -> {
                        setColor(ContextCompat.getColor(binding.root.context, R.color.myRedDark))
                        binding.tvStatus.setTextColor(Color.WHITE)
                    }
                    "selesai", "done" -> {
                        setColor(ContextCompat.getColor(binding.root.context, R.color.myGreenDark))
                        binding.tvStatus.setTextColor(Color.BLACK)
                    }
                    else -> {
                        setColor(Color.GRAY)
                        binding.tvStatus.setTextColor(Color.WHITE)
                    }
                }
            }
            binding.tvStatus.background = shape
        }
    }
}