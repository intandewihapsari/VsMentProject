package com.indri.vsmentproject.ui.manager.masterdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.databinding.ItemNotifikasiBinding

class NotifikasiAdapter(
    private val isManager: Boolean = true, // Default true karena ini di folder manager
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

        holder.binding.apply {
            // =========================
            // DATA UTAMA
            // =========================
            tvNamaTugas.text = data.judul
            tvNamaVilla.text = data.pesan // Tambahin ini biar pesannya muncul
            tvWaktu.text = data.waktu // Munculin waktu kirim

            // =========================
            // NAMA VILLA
            // =========================
            tvNamaVilla.text = if (data.villa_nama.isNotEmpty()) data.villa_nama else "Umum"

            // =========================
            // MODE MANAGER vs STAFF
            // =========================
            if (isManager) {
                // ❌ Manager: Cukup lihat riwayat, hide checkbox
                cbRead.visibility = View.GONE

                // Status buat manager: kalau sudah dibaca staff, bikin agak transparan
                holder.itemView.alpha = if (data.is_read) 0.6f else 1f
            } else {
                // ✅ Staff: Mode operasional, munculkan checkbox
                cbRead.visibility = View.VISIBLE

                if (data.is_read) {
                    cbRead.setImageResource(R.drawable.ic_circle_checked)
                    holder.itemView.alpha = 0.5f
                } else {
                    cbRead.setImageResource(R.drawable.ic_circle_outline)
                    holder.itemView.alpha = 1.0f
                }

                // Klik icon checkbox saja
                cbRead.setOnClickListener {
                    onClick(data)
                }
            }

            // =========================
            // CLICK ITEM (ROOT)
            // =========================
            root.setOnClickListener {
                onClick(data)
            }
        }
    }

    fun updateList(newList: List<NotifikasiModel>) {
        list = newList
        notifyDataSetChanged()
    }
}