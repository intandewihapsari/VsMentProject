package com.indri.vsmentproject.ui.manager.task

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding
import com.indri.vsmentproject.databinding.ItemVillaGroupBinding
import java.text.SimpleDateFormat
import java.util.*
class TugasVillaAdapter(
    private val onItemClick: (TugasModel) -> Unit,
    private val onItemLongClick: (TugasModel) -> Unit
) : RecyclerView.Adapter<TugasVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun updateList(newList: List<VillaTugasGroup>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVillaGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]

        holder.binding.tvNamaVilla.text = group.namaVilla
        holder.binding.containerTugasVilla.removeAllViews()

        val inflater = LayoutInflater.from(holder.binding.root.context)

        val tugasPerVilla = group.listTugas.groupBy { it.villa_nama }

        tugasPerVilla.forEach { (namaVilla, tasks) ->

            val tvSubVilla = TextView(holder.binding.root.context).apply {
                text = namaVilla
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(0, 20, 0, 8)
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            holder.binding.containerTugasVilla.addView(tvSubVilla)

            tasks.forEach { tugas ->

                val itemBinding = ItemTugasPendingListBinding.inflate(
                    inflater,
                    holder.binding.containerTugasVilla,
                    false
                )

                itemBinding.tvNamaTugas.text = "[${tugas.ruangan}] ${tugas.tugas}"
                itemBinding.tvPIC.text = tugas.staff_name





                itemBinding.root.setOnClickListener {
                    onItemClick(tugas)
                }

                itemBinding.root.setOnLongClickListener {
                    onItemLongClick(tugas)
                    true
                }

                holder.binding.containerTugasVilla.addView(itemBinding.root)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemVillaGroupBinding) :
        RecyclerView.ViewHolder(binding.root)
}