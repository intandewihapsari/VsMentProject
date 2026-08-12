package com.indri.vsmentproject.ui.manager.template

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.databinding.ItemTaskTemplateBinding

class TemplateAdapter(
    private val onApplyClick: (TaskTemplateModel) -> Unit,
    private val onEditClick: (TaskTemplateModel) -> Unit,
    private val onDeleteClick: (TaskTemplateModel) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    private val listTemplate = mutableListOf<TaskTemplateModel>()

    fun setData(newList: List<TaskTemplateModel>) {
        listTemplate.clear()
        listTemplate.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskTemplateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTemplate[position])
    }

    override fun getItemCount(): Int = listTemplate.size

    inner class ViewHolder(private val binding: ItemTaskTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: TaskTemplateModel) {
            binding.tvNamaTemplate.text = data.nama_template
            binding.tvDeskripsiTemplate.text = data.deskripsi
            binding.tvJumlahTugas.text = "${data.list_tugas_item.size} Tugas"

            binding.btnTerapkan.setOnClickListener { onApplyClick(data) }
            binding.btnEdit.setOnClickListener { onEditClick(data) }
            binding.btnHapus.setOnClickListener { onDeleteClick(data) }
        }
    }
}