package com.indri.vsmentproject.UI.tugas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.Data.Model.TugasModel
import com.indri.vsmentproject.Data.Model.tugas.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding
import com.indri.vsmentproject.databinding.ItemVillaGroupBinding

class TugasVillaAdapter(
    // 1. Tambahkan parameter callback klik di sini
    private val onItemClick: (TugasModel) -> Unit
) : RecyclerView.Adapter<TugasVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun updateList(newList: List<VillaTugasGroup>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVillaGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]
        holder.binding.tvNamaVilla.text = group.namaVilla

        // Bersihkan view lama sebelum menambah yang baru (agar tidak double saat scroll)
        holder.binding.containerTugasVilla.removeAllViews()

        val inflater = LayoutInflater.from(holder.binding.root.context)

        group.listTugas.forEach { tugas ->
            val itemBinding = ItemTugasPendingListBinding.inflate(inflater, holder.binding.containerTugasVilla, false)

            itemBinding.tvNamaTugas.text = tugas.tugas
            itemBinding.tvStatus.text = "Status: ${tugas.status}"
            itemBinding.tvPIC.text = "Staff: ${tugas.staff_nama}"

            // --- 2. TARUH DI SINI ---
            // Klik pada satu baris tugas akan memicu detail
            itemBinding.root.setOnClickListener {
                onItemClick(tugas)
            }

            holder.binding.containerTugasVilla.addView(itemBinding.root)
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemVillaGroupBinding) : RecyclerView.ViewHolder(binding.root)
}