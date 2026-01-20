package com.indri.vsmentproject.ui.manager.viewholder

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class TugasPendingViewHolder(private val binding: ItemTugasPendingBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(list: List<TugasModel>, onTugasClick: (TugasModel) -> Unit) {
        binding.containerListTugas.removeAllViews()

        // Batasi hanya 3 tugas terbaru agar dashboard tetap rapi
        list.take(3).forEach { tugas ->
            val itemBinding = ItemTugasPendingListBinding.inflate(
                LayoutInflater.from(binding.root.context),
                binding.containerListTugas,
                false
            )
            itemBinding.tvNamaTugas.text = tugas.tugas
            itemBinding.tvStatus.text = tugas.status
            itemBinding.tvPIC.text = tugas.worker_name

            itemBinding.root.setOnClickListener { onTugasClick(tugas) }
            binding.containerListTugas.addView(itemBinding.root)
        }
    }
}