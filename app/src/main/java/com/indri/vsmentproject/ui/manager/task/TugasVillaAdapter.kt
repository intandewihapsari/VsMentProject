package com.indri.vsmentproject.ui.manager.task

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.databinding.*

class TugasVillaAdapter(
    private val onItemClick: (TugasModel) -> Unit,
    private val onItemLongClick: (TugasModel) -> Unit
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
        holder.binding.tvNamaVilla.text = group.namaVilla // Sekarang berisi Nama Asli Villa
        holder.binding.containerTugasVilla.removeAllViews()

        val inflater = LayoutInflater.from(holder.binding.root.context)

        group.listTugas.forEach { tugas ->
            val itemBinding = ItemTugasPendingListBinding.inflate(inflater, holder.binding.containerTugasVilla, false)

            // Detail Sangat Jelas
            itemBinding.tvNamaTugas.text = "[${tugas.ruangan}] ${tugas.tugas}"
            itemBinding.tvStatus.text = "Status: ${tugas.status.uppercase()}"
            itemBinding.tvPIC.text = "Staff: ${tugas.worker_name} | Prioritas: ${tugas.prioritas}"

            itemBinding.root.setOnClickListener { onItemClick(tugas) }
            itemBinding.root.setOnLongClickListener {
                onItemLongClick(tugas)
                true
            }

            holder.binding.containerTugasVilla.addView(itemBinding.root)
        }
    }

    override fun getItemCount() = items.size
    class ViewHolder(val binding: ItemVillaGroupBinding) : RecyclerView.ViewHolder(binding.root)
}