package com.indri.vsmentproject.ui.manager.viewholder

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiUrgentBinding

class NotifikasiUrgentViewHolder(
    private val binding: ItemNotifikasiUrgentBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notif: NotifikasiModel) {
        val context: Context = binding.root.context

        binding.tvJudulJadwal.text = notif.judul
        binding.tvTime.text = notif.pesan
        binding.tvVilla.text = notif.villa_nama.ifEmpty { "-" }

        // PERBAIKAN: Tambah penanganan tipe "habis" agar warnanya serasi dengan dashboard (Orange/Kuning)
        when (notif.tipe?.lowercase()) {
            "rusak" -> {
                binding.cardIsi.setCardBackgroundColor(ContextCompat.getColor(context, R.color.myRedDark))
            }
            "hilang" -> {
                binding.cardIsi.setCardBackgroundColor(ContextCompat.getColor(context, R.color.myOrangeDark))
            }
            "habis" -> {
                binding.cardIsi.setCardBackgroundColor(ContextCompat.getColor(context, R.color.myOrangeDark)) // Atau warna kuning custommu
            }
            else -> {
                binding.cardIsi.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
        }
    }
}