package com.indri.vsmentproject.ui.manager.task

import android.graphics.Color
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.*
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding

class TugasFlatAdapter(
    private val onClick: (TugasModel) -> Unit,
    private val onLongClick: (TugasModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private var items = listOf<TugasListItem>()

    fun submitData(groups: List<VillaTugasGroup>) {
        val temp = mutableListOf<TugasListItem>()

        groups.forEach { group ->
            temp.add(TugasListItem.Header(group.namaVilla))
            group.listTugas.forEach {
                temp.add(TugasListItem.Item(it))
            }
        }

        items = temp
        notifyDataSetChanged()
    }

    fun getTaskAt(position: Int): TugasModel? {
        return (items.getOrNull(position) as? TugasListItem.Item)?.tugas
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TugasListItem.Header -> TYPE_HEADER
            is TugasListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_tugas, parent, false)
            HeaderVH(view)
        } else {
            val binding = ItemTugasPendingListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemVH(binding)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {

            is TugasListItem.Header -> {
                (holder as HeaderVH).bind(item)
            }

            is TugasListItem.Item -> {
                (holder as ItemVH).bind(item.tugas)
            }
        }
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: TugasListItem.Header) {
            itemView.findViewById<TextView>(R.id.tvHeader).text = item.title
        }
    }

    inner class ItemVH(val binding: ItemTugasPendingListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tugas: TugasModel) {

            binding.tvVillaName.text = tugas.villa_nama
            binding.tvNamaTugas.text = "${tugas.tugas} - ${tugas.ruangan}"
            binding.tvPIC.text = tugas.staff_name

            binding.tvStatus?.apply {
                text = tugas.status

                val bg = context.getDrawable(R.drawable.bg_status)?.mutate()

                when (tugas.status) {
                    "pending" -> {
                        bg?.setTint(ContextCompat.getColor(context, R.color.myRedDark))
                        setTextColor(Color.WHITE)
                    }
                    "selesai" -> {
                        bg?.setTint(ContextCompat.getColor(context, R.color.myGreenDark))
                        setTextColor(Color.BLACK)
                    }
                }

                background = bg
            }

            binding.root.setOnClickListener { onClick(tugas) }
            binding.root.setOnLongClickListener {
                onLongClick(tugas)
                true
            }
        }
    }
}