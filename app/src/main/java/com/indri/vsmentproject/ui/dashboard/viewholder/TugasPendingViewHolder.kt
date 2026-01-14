package com.indri.vsmentproject.ui.dashboard.viewholder

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class TugasPendingViewHolder(private val binding: ItemTugasPendingBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<TugasModel>, onTugasClick: (TugasModel) -> Unit) {
        // Membersihkan container agar tidak terjadi duplikasi saat di-scroll
        binding.containerListTugas.removeAllViews()

        val inflater = LayoutInflater.from(binding.root.context)

        // Melakukan looping data tugas untuk dimasukkan ke dalam LinearLayout
        list.forEach { tugas ->
            // Inflate layout item list tugas (item_tugas_pending_list.xml)
            val itemBinding = ItemTugasPendingListBinding.inflate(inflater, binding.containerListTugas, false)

            // Set data ke TextView sesuai ID di XML item_tugas_pending_list
            itemBinding.tvNamaTugas.text = tugas.tugas
            itemBinding.tvStatus.text = "Status: ${tugas.status}"
            itemBinding.tvPIC.text = "Staff: ${tugas.staff_nama}"

            // Memberikan aksi klik pada setiap baris tugas
            itemBinding.root.setOnClickListener {
                onTugasClick(tugas)
            }

            // Menambahkan view yang sudah di-inflate ke dalam container
            binding.containerListTugas.addView(itemBinding.root)
        }
    }
}