package com.indri.vsmentproject.UI.dashboard.viewholder

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.Data.Model.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding // Import Anak

// PERBAIKAN: Constructor harus menggunakan ItemTugasPendingBinding (Kartu Besar)
class TugasPendingViewHolder(val binding: ItemTugasPendingListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<TugasModel>) {
        // 1. Bersihkan kontainer agar data tidak menumpuk saat di-scroll
        binding.containerListTugas.removeAllViews()

        // 2. Siapkan inflater untuk menggambar baris-baris kecil
        val inflater = LayoutInflater.from(binding.root.context)

        // 3. Masukkan maksimal 5 tugas saja agar tidak kepanjangan
        list.take(5).forEach { data ->
            // Gambar layout baris kecil (Anak) ke dalam kontainer (Induk)
            val itemBinding = ItemTugasPendingListBinding.inflate(
                inflater,
                binding.containerListTugas,
                false
            )

            // 4. Set isi datanya
            itemBinding.tvNamaTugas.text = data.tugas
            itemBinding.tvStatus.text = "Status : ${if(data.status == "pending") "Menunggu" else data.status}"
            itemBinding.tvPIC.text = "Penanggung Jawab : ${data.staff_nama}"

            // 5. Masukkan baris yang sudah jadi ke dalam kontainer
            binding.containerListTugas.addView(itemBinding.root)
        }
    }
}