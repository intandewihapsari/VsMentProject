package com.indri.vsmentproject.ui.manager.viewholder

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingBinding
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class TugasPendingViewHolder(private val binding: ItemTugasPendingBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: List<TugasModel>, onTugasClick: (TugasModel) -> Unit) {
        // 1. WAJIB: Hapus semua view lama agar tidak menumpuk saat di-scroll
        binding.containerListTugas.removeAllViews()

        // 2. Ambil Top 5 (sesuai request kamu tadi)
        val top5 = list.take(5)

        top5.forEachIndexed { index, tugas ->
            // 3. Inflate layout per baris
            val itemBinding = ItemTugasPendingListBinding.inflate(
                LayoutInflater.from(binding.root.context),
                binding.containerListTugas,
                false
            )

            // 4. Set Data ke TextView
            itemBinding.tvNamaTugas.text = tugas.tugas
            itemBinding.tvStatus.text = "Status: ${tugas.status}"
            itemBinding.tvPIC.text = "PIC: ${tugas.worker_name}"

            // 5. Logika Klik: Direct ke detail
            itemBinding.root.setOnClickListener {
                onTugasClick(tugas)
            }

            // 6. Tambahkan garis pemisah (divider) kecuali untuk item terakhir
            binding.containerListTugas.addView(itemBinding.root)

            if (index < top5.size - 1) {
                val divider = View(binding.root.context)
                divider.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                divider.setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"))
                binding.containerListTugas.addView(divider)
            }
        }
    }
}