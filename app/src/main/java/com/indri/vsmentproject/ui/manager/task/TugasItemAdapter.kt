package com.indri.vsmentproject.ui.manager.task

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class TugasItemAdapter(
    private val onEdit: (TugasModel) -> Unit,
    private val onDelete: (TugasModel) -> Unit,
    private val showStatus: Boolean = true

) : RecyclerView.Adapter<TugasItemAdapter.ViewHolder>() {

    private var items = listOf<TugasModel>()

    fun updateList(newList: List<TugasModel>) {
        items = newList
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int) = items[position]

    inner class ViewHolder(val binding: ItemTugasPendingListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTugasPendingListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tugas = items[position]
//        holder.binding.tvVillaName.visibility = View.GONE

        holder.binding.tvVillaName.text = tugas.villa_nama
        holder.binding.tvNamaTugas.text = "${tugas.tugas} - ${tugas.ruangan}"
        holder.binding.tvPIC.text = tugas.staff_name

        // 👇 INI BAGIAN PENTING
        if (showStatus) {
            holder.binding.tvStatus.visibility = View.VISIBLE
            holder.binding.tvStatus.apply {
                text = tugas.status

                when (tugas.status) {
                    "pending" -> {
                        holder.binding.tvStatus.backgroundTintList =
                            ColorStateList.valueOf(context.getColor(R.color.myRedDark))
                        holder.binding.tvStatus.setTextColor(Color.WHITE)
                    }

                    "selesai" -> {
                        holder.binding.tvStatus.backgroundTintList =
                            ColorStateList.valueOf(context.getColor(R.color.myGreenLight))
                        holder.binding.tvStatus.setTextColor(Color.BLACK)
                    }
                }
            }
        } else {
            holder.binding.tvStatus.visibility = View.GONE
        }

        holder.binding.root.setOnClickListener {
            onEdit(tugas)
        }
    }
}