package com.indri.vsmentproject.ui.staff.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasLinearBinding

class TugasChildAdapter(
    private val listTugas: List<TugasModel>,
    private val onDone: (TugasModel) -> Unit,
    private val onReport: (TugasModel) -> Unit,
    private val isLastTask: Boolean
) : RecyclerView.Adapter<TugasChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(val binding: ItemTugasLinearBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = ItemTugasLinearBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val tugas = listTugas[position]

        holder.binding.apply {

            // 🔹 Nama tugas + lokasi
            tvNamaTugas.text = tugas.tugas
            tvNamaVilla.text = "${tugas.villa_nama} - ${tugas.ruangan}"

            // 🔹 Checkbox status
            cbTugas.isChecked = tugas.status == "selesai"

            // 🔹 UI kondisi selesai / belum
            if (tugas.status == "selesai") {
                tvNamaTugas.paintFlags =
                    tvNamaTugas.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                root.alpha = 0.6f
                containerAlertSmall.visibility = View.GONE
            } else {
                tvNamaTugas.paintFlags =
                    tvNamaTugas.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                root.alpha = 1.0f
                containerAlertSmall.visibility = View.VISIBLE
            }

            // 🔥 CLICK CHECKBOX
            cbTugas.setOnClickListener {

                if (isLastTask && tugas.status != "selesai") {
                    // 🚨 tugas terakhir → wajib upload bukti
                    Toast.makeText(
                        root.context,
                        "Tugas terakhir! Upload bukti dulu ya 📸",
                        Toast.LENGTH_SHORT
                    ).show()

                    onReport(tugas) // arah ke upload
                } else {
                    onDone(tugas) // langsung selesai biasa
                }
            }

            // 🔔 Tombol laporan / alert (dipakai juga buat upload sekarang)
            containerAlertSmall.setOnClickListener {
                onReport(tugas)
            }
        }
    }

    override fun getItemCount(): Int = listTugas.size
}