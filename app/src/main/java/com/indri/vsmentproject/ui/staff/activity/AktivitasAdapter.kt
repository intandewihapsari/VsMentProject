package com.indri.vsmentproject.ui.staff.activity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemAktivitasBinding
import java.text.SimpleDateFormat
import java.util.*

class AktivitasAdapter(
    private var list: List<Any>,
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<AktivitasAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAktivitasBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAktivitasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        with(holder.binding) {

            when (item) {

                // 🔵 TUGAS
                is TugasModel -> {
                    val color = ContextCompat.getColor(context, R.color.myBlueDark)

                    viewIndicator.setBackgroundColor(color)
                    tvLabel.text = "Tugas Selesai"
                    tvLabel.setTextColor(color)

                    tvDesc.text = item.tugas
                    tvLocation.text = "Ruangan: ${item.ruangan}, ${item.villa_nama}"
                    tvDateTime.text = formatTimestamp(item.completed_at)
                    tvDateTime.setTextColor(color)
                }

                // 🔴 LAPORAN
                is LaporanModel -> {

                    val (color, label) = when (item.tipe_laporan?.lowercase()) {

                        "rusak" -> Pair(
                            ContextCompat.getColor(context, R.color.myRedDark),
                            "Kerusakan"
                        )

                        "hilang" -> Pair(
                            ContextCompat.getColor(context, R.color.myOrangeDark),
                            "Hilang"
                        )

                        "habis" -> Pair(
                            ContextCompat.getColor(context, R.color.myGreen),
                            "Stok Habis"
                        )

                        else -> Pair(
                            ContextCompat.getColor(context, android.R.color.darker_gray),
                            "Lainnya"
                        )
                    }

                    viewIndicator.setBackgroundColor(color)
                    tvLabel.text = "Laporan $label"
                    tvLabel.setTextColor(color)

                    tvDesc.text = item.deskripsi
                    tvLocation.text = "${item.area}, ${item.villa_nama}"
                    tvDateTime.text = formatWaktuLapor(item.waktu_lapor)
                    tvDateTime.setTextColor(color)
                }
            }

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    /**
     * Fungsi untuk mengubah Milidetik (Long) menjadi format: 12.35 | Senin, 5 Des
     */
    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "-"
        return try {
            val date = Date(timestamp)
            // Menggunakan Locale Indonesia agar nama hari dalam Bahasa Indonesia
            val sdf = SimpleDateFormat("HH.mm | EEEE, d MMM", Locale("id", "ID"))
            sdf.format(date)
        } catch (e: Exception) {
            "-"
        }
    }
    private fun formatWaktuLapor(waktu: String): String {
        return try {
            // format dari Firebase
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            // format yang kamu mau
            val formatter = SimpleDateFormat("HH.mm | EEEE, d MMM", Locale("id", "ID"))

            val date = parser.parse(waktu)
            if (date != null) formatter.format(date) else "-"
        } catch (e: Exception) {
            "-"
        }
    }

    fun updateData(newList: List<Any>) {
        list = newList
        notifyDataSetChanged()
    }
}