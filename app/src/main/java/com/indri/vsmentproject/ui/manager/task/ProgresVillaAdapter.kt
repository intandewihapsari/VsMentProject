package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding

class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        items = newList
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Kirim SATU GRUP (VillaTugasGroup) ke ViewHolder, bukan cuma satu tugas
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemProgresVillaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: VillaTugasGroup) {
            binding.apply {
                // 1. Set Nama Villa
                tvNamaVilla.text = group.namaVilla

                // 2. Hitung Selesai & Pending dari list tugas di dalam grup ini
                val selesai = group.listTugas.count { it.status.equals("selesai", true) }
                val pending = group.listTugas.count { it.status.equals("pending", true) }

                tvSelesaiCount.text = "Selesai : $selesai"
                tvPendingCount.text = "Pending : $pending"

                // 3. Set Progress Bar & Persentase teks
                val progressInt = group.persentase_selesai.replace("%", "").toIntOrNull() ?: 0
                pbProgres.progress = progressInt
                tvPersen.text = group.persentase_selesai

                // 4. LOAD FOTO STAFF (Ambil dari tugas pertama di villa ini)
                val tugasPertama = group.listTugas.firstOrNull()
                if (tugasPertama != null) {
                    Glide.with(binding.root.context)
                        .load(tugasPertama?.worker_photo)
                        .circleCrop()
                        // Pakai icon bawaan sistem Android (gambar foto/image)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivStaffProfile)
                }
            }
        }
    }
}