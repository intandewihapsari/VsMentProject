package com.indri.vsmentproject.ui.manager.masterdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiBinding

class NotifikasiAdapter(
    private val isManager: Boolean = false,
    private val onClick: (NotifikasiModel) -> Unit
) : RecyclerView.Adapter<NotifikasiAdapter.ViewHolder>() {

    private var list = listOf<NotifikasiModel>()

    inner class ViewHolder(val binding: ItemNotifikasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list[position]

        // =========================
        // JUDUL
        // =========================
        holder.binding.tvNamaTugas.text = data.judul

        // =========================
        // NAMA VILLA
        // =========================
        holder.binding.tvNamaVilla.text =
            if (data.villa_nama.isNotEmpty()) data.villa_nama else "Umum"

        // =========================
        // MODE MANAGER vs STAFF
        // =========================
        if (isManager) {
            // ❌ Manager: ga perlu checkbox
            holder.binding.cbRead.visibility = View.GONE
        } else {
            // ✅ Staff: pakai checkbox
            holder.binding.cbRead.visibility = View.VISIBLE

            if (data.is_read) {
                holder.binding.cbRead.setImageResource(R.drawable.ic_circle_checked)
            } else {
                holder.binding.cbRead.setImageResource(R.drawable.ic_circle_outline)
            }

            // klik checkbox → mark as read (optional)
            holder.binding.cbRead.setOnClickListener {
                onClick(data)
            }
        }

        // =========================
        // EFFECT (BIAR KEREN)
        // =========================
        holder.itemView.alpha = if (data.is_read && !isManager) 0.5f else 1f

        // =========================
        // CLICK ITEM
        // =========================
        holder.itemView.setOnClickListener {
            onClick(data)
        }
    }

    // =========================
    fun updateList(newList: List<NotifikasiModel>) {
        list = newList
        notifyDataSetChanged()
    }
}