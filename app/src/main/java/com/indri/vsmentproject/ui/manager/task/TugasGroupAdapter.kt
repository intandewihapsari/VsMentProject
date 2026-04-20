package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemGroupTugasBinding

class TugasGroupAdapter(
    private val onEdit: (TugasModel) -> Unit,
    private val onDelete: (TugasModel) -> Unit
) : RecyclerView.Adapter<TugasGroupAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun updateList(newList: List<VillaTugasGroup>) {
        items = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemGroupTugasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Pastikan layout manager sudah terpasang
            binding.rvInner.layoutManager = LinearLayoutManager(itemView.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGroupTugasBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]
        holder.binding.tvHeader.text = group.namaVilla

        // 1. Setup Adapter untuk list tugas di dalam group
        val innerAdapter = TugasItemAdapter(onEdit, onDelete)
        holder.binding.rvInner.adapter = innerAdapter
        innerAdapter.updateList(group.listTugas)

        // 2. PASANG SWIPE DI SINI (Untuk rvInner, bukan root group)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Ambil data tugas dari innerAdapter berdasarkan posisi yang di-swipe
                val tugas = innerAdapter.getItemAt(viewHolder.adapterPosition)

                if (direction == ItemTouchHelper.RIGHT) {
                    onEdit(tugas)
                } else {
                    onDelete(tugas)
                }

                // Beritahu adapter bahwa item berubah agar UI kembali normal setelah swipe
                innerAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }
        }

        // Penting: Hapus callback lama jika ada (opsional) dan pasang yang baru
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(holder.binding.rvInner)
    }
}