package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding

class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProgresVillaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]
        holder.bind(group)
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemProgresVillaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: VillaTugasGroup) {
            binding.apply {
                tvNamaVilla.text = group.namaVilla

                // Hitung jumlah tugas secara dinamis
                val selesai = group.listTugas.count { it.status.equals("selesai", true) }
                val pending = group.listTugas.count { it.status.equals("pending", true) }

                tvSelesaiCount.text = "Selesai : $selesai"
                tvPendingCount.text = "Pending : $pending"

                // Set Persentase (Menghilangkan tanda % untuk Progressbar)
                val progressInt = group.persentase_selesai.replace("%", "").toIntOrNull() ?: 0
                pbProgres.progress = progressInt
                tvPersen.text = group.persentase_selesai
            }
        }
    }
}