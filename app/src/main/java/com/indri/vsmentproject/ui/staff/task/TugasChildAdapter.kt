package com.indri.vsmentproject.ui.staff.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
// PASTIKAN IMPORT BINDINGNYA SUDAH BENAR KE LINEAR
import com.indri.vsmentproject.databinding.ItemTugasLinearBinding

class TugasChildAdapter(
    private val listTugas: List<TugasModel>,
    private val onDone: (TugasModel) -> Unit,
    private val onReport: (TugasModel) -> Unit
) : RecyclerView.Adapter<TugasChildAdapter.ChildViewHolder>() {

    // Gunakan ItemTugasLinearBinding di sini
    inner class ChildViewHolder(val binding: ItemTugasLinearBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        // Inflate menggunakan layout item_tugas_linear
        val binding = ItemTugasLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val tugas = listTugas[position]
        holder.binding.apply {
            // 1. Set Teks Utama & Nama Villa + Ruangan
            tvNamaTugas.text = tugas.tugas
            tvNamaVilla.text = "${tugas.villa_nama} - ${tugas.ruangan}"

            // 2. Set Status Checkbox (Ijo)
            cbTugas.isChecked = tugas.status == "selesai"

            // 3. Logika Visual (Coretan & Transparansi)
            if (tugas.status == "selesai") {
                tvNamaTugas.paintFlags = tvNamaTugas.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                root.alpha = 0.6f
                // Sembunyikan sirene (ivAlertSmall) kalau sudah selesai
                containerAlertSmall.visibility = View.GONE
            } else {
                tvNamaTugas.paintFlags = tvNamaTugas.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                root.alpha = 1.0f
                containerAlertSmall.visibility = View.VISIBLE
            }

            // 4. Listener untuk Checkbox (Klik Ijo)
            cbTugas.setOnClickListener {
                onDone(tugas)
            }

            // 5. Listener untuk Tombol Sirene (Lapor)
            // Pakai ID ivAlertSmall karena kamu belum pakai container
            containerAlertSmall.setOnClickListener {
                onReport(tugas)
            }
        }
    }

    override fun getItemCount(): Int = listTugas.size
}