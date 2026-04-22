package com.indri.vsmentproject.ui.manager.report

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.databinding.ItemAktivitasBinding

class LaporanAdapter(
    private val onClick: (LaporanModel) -> Unit
) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    private var items = listOf<LaporanModel>()

    fun updateList(newList: List<LaporanModel>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAktivitasBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 🎨 WARNA BERDASARKAN TIPE
        val (color, label) = when (item.tipe_laporan.lowercase()) {

            "rusak" -> Pair(
                ContextCompat.getColor(context, R.color.myRedDark),
                "Kerusakan"
            )

            "hilang" -> Pair(
                ContextCompat.getColor(context, R.color.myOrangeDark),
                "Hilang"
            )

            "habis" -> Pair(
                ContextCompat.getColor(context, R.color.myBlueDark),
                "Stok Habis"
            )

            else -> Pair(
                ContextCompat.getColor(context, android.R.color.darker_gray),
                "Lainnya"
            )
        }

        holder.binding.apply {

            tvLabel.text = label
            tvLabel.setTextColor(color)

            viewIndicator.setBackgroundColor(color)

            tvDesc.text = item.nama_barang
            tvLocation.text = "${item.villa_nama} • ${item.staff_nama}"
            tvDateTime.text = item.waktu_lapor

            // ✅ INI YANG BENAR
            root.setOnClickListener {
                Log.d("CLICK_TEST", "Item diklik: ${item.nama_barang}")
                onClick(item)
            }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemAktivitasBinding) :
        RecyclerView.ViewHolder(binding.root)
}