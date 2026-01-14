package com.indri.vsmentproject.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding
import com.indri.vsmentproject.databinding.ItemVillaGroupBinding

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
        holder.binding.tvNamaVilla.text = group.namaVilla
        holder.binding.containerTugasVilla.removeAllViews()
        val inflater = LayoutInflater.from(holder.binding.root.context)

        group.listTugas.forEach { tugas ->
            val itemBinding = ItemTugasPendingListBinding.inflate(inflater, holder.binding.containerTugasVilla, false)
            itemBinding.tvNamaTugas.text = tugas.tugas
            itemBinding.tvStatus.text = "Status: ${tugas.status}"
            itemBinding.tvPIC.text = "Staff: ${tugas.staff_nama}"

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