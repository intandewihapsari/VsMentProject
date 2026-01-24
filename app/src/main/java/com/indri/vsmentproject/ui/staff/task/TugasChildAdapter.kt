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
        holder.binding.tvNamaTugas.text = tugas.tugas

        // Atur warna atau status jika sudah selesai
        if (tugas.status == "selesai") {
            holder.binding.btnDone.setImageResource(R.drawable.ic_circle_checked) // Hijau penuh
        } else {
            holder.binding.btnDone.setImageResource(R.drawable.ic_circle_outline)
        }

        holder.binding.btnDone.setOnClickListener { onDone(tugas) }
        holder.binding.btnReport.setOnClickListener { onReport(tugas) }
    }

    override fun getItemCount(): Int = listTugas.size
}