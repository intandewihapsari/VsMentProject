package com.indri.vsmentproject.ui.dashboard.viewholder

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding // Import Anak

// PERBAIKAN: Constructor harus menggunakan ItemTugasPendingBinding (Kartu Besar)
class TugasPendingViewHolder(val binding: ItemTugasPendingBinding) :
    RecyclerView.ViewHolder(binding.root) {

    // Parameter harus List<TugasModel>
    fun bind(list: List<TugasModel>) {
        binding.containerListTugas.removeAllViews()

        val inflater = LayoutInflater.from(binding.root.context)

        list.forEach { data ->
            val itemBinding = ItemTugasPendingListBinding.inflate(inflater, binding.containerListTugas, false)

            itemBinding.tvNamaTugas.text = data.tugas
            itemBinding.tvStatus.text = "Status : ${data.status}"
            itemBinding.tvPIC.text = "Penanggung Jawab : ${data.staff_nama}"

            binding.containerListTugas.addView(itemBinding.root)
        }
    }
}