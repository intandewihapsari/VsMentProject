package com.indri.vsmentproject.ui.staff.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasChildBinding

class TugasChildAdapter(
    private val listTugas: List<TugasModel>,
    private val onDone: (TugasModel) -> Unit,
    private val onReport: (TugasModel) -> Unit
) : RecyclerView.Adapter<TugasChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(val binding: ItemTugasChildBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = ItemTugasChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val tugas = listTugas[position]
        holder.binding.apply {
            tvNamaTugas.text = tugas.tugas
            tvNamaVilla.text = tugas.villa_nama

            // --- LOGIKA VISUAL UNTUK TUGAS SELESAI ---
            if (tugas.status == "selesai") {
                // 1. Ganti icon jadi centang hijau/penuh
                btnDone.setImageResource(R.drawable.ic_circle_checked)

                // 2. Tambahkan efek coretan pada teks (Strikethrough)
                tvNamaTugas.paintFlags = tvNamaTugas.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                // 3. Buat tampilan agak transparan/redup
                root.alpha = 0.5f

                // 4. Matikan klik pada tombol done agar tidak bisa diklik ulang
                btnDone.isEnabled = false

                // 5. Sembunyikan tombol lapor jika sudah selesai (Opsional)
                btnReport.visibility = View.GONE
            } else {
                // TAMPILAN NORMAL (PENDING)
                btnDone.setImageResource(R.drawable.ic_circle_outline)

                // Hilangkan efek coretan jika statusnya pending
                tvNamaTugas.paintFlags = tvNamaTugas.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                root.alpha = 1.0f
                btnDone.isEnabled = true
                btnReport.visibility = View.VISIBLE
            }

            // --- CLICK LISTENERS ---
            btnDone.setOnClickListener {
                // Panggil callback onDone yang ada di JadwalPentingActivity
                onDone(tugas)
            }

            btnReport.setOnClickListener {
                // Panggil callback onReport untuk pindah ke LaporanStaffFragment
                onReport(tugas)
            }
        }
    }

    override fun getItemCount(): Int = listTugas.size
}