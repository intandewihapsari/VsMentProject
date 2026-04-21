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
        // Secara default, pastikan semua isExpanded = false saat data pertama kali masuk
        this.originalGroups = villaGroups
        generateFlatItems()
    }

    private fun generateFlatItems() {
        flatItems.clear()
        originalGroups.forEach { group ->
            flatItems.add(group) // Tambah Header Villa
            if (group.isExpanded) {
                flatItems.addAll(group.listTugas) // Tambah Tugas hanya jika expanded
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
            // Pastikan layout item_villa_parent punya ID tvNamaVilla, tvProgress, ivChevron
            HeaderVH(inflater.inflate(R.layout.item_villa_parent, parent, false))
        } else {
            val binding = ItemTugasPendingListBinding.inflate(inflater, parent, false)
            ItemVH(binding)
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
            // Gunakan ?. (safe call) untuk menghindari NullPointerException
            val tvNama = itemView.findViewById<TextView>(R.id.tvNamaVilla)
            val tvProgress = itemView.findViewById<TextView>(R.id.tvProgress)
            val ivChevron = itemView.findViewById<ImageView>(R.id.ivChevron)

            tvNama?.text = group.namaVilla
            tvProgress?.text = "${group.tugasSelesai} / ${group.totalTugas} Tugas Selesai"

            // Animasi rotasi chevron
            ivChevron?.rotation = if (group.isExpanded) 180f else 0f

            itemView.setOnClickListener {
                group.isExpanded = !group.isExpanded
                generateFlatItems() // Refresh list untuk munculkan/sembunyikan item
            }
        }
    }

    inner class ItemVH(val binding: ItemTugasPendingListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tugas: TugasModel) {
            binding.tvVillaName.visibility = View.GONE
            binding.tvNamaTugas.text = "${tugas.tugas} - ${tugas.ruangan}"
            binding.tvPIC.text = tugas.staff_name

            val statusData = tugas.status.trim().lowercase()
            binding.tvStatus.text = statusData

            // 1. Tentukan warna berdasarkan status
            val shape = GradientDrawable().apply {
                cornerRadius = 50f

                when (statusData) {
                    "pending" -> {
                        setColor(ContextCompat.getColor(binding.root.context, R.color.myRedDark))
                        binding.tvStatus.setTextColor(Color.WHITE) // Pending -> Teks Putih
                    }
                    "selesai", "done" -> {
                        setColor(ContextCompat.getColor(binding.root.context, R.color.myGreenDark))
                        binding.tvStatus.setTextColor(Color.BLACK) // Selesai -> Teks Hitam
                    }
                    else -> {
                        setColor(Color.GRAY)
                        binding.tvStatus.setTextColor(Color.WHITE) // Lainnya -> Teks Putih
                    }
                }
            }

            binding.tvStatus.background = shape

            // HAPUS baris setTextColor yang di paling bawah tadi!
        }
    }
}