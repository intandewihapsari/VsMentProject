package com.indri.vsmentproject.ui.staff.task

import android.view.LayoutInflater
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

            // Menampilkan nama villa agar staff tahu lokasi tugas
            // Pastikan di item_tugas_child.xml ada TextView dengan id tvVillaTugas
            tvNamaVilla.text = tugas.villa_nama

            if (tugas.status == "selesai") {
                btnDone.setImageResource(R.drawable.ic_circle_checked)
            } else {
                btnDone.setImageResource(R.drawable.ic_circle_outline)
            }

            btnDone.setOnClickListener { onDone(tugas) }
            btnReport.setOnClickListener { onReport(tugas) }
        }
    }

    override fun getItemCount(): Int = listTugas.size
}