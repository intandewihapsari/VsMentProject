package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasSimpleBinding

class TugasSimpleAdapter(
    private val list: List<TugasModel>
) : RecyclerView.Adapter<TugasSimpleAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTugasSimpleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTugasSimpleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tugas = list[position]

        // 🔥 tampilin nama + status
        holder.binding.tvNamaTugas.text =
            if (tugas.status.equals("selesai", true)) {
                "✔ ${tugas.tugas}"
            } else {
                "❌ ${tugas.tugas}"
            }
    }
}