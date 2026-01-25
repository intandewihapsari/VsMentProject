package com.indri.vsmentproject.ui.staff.activity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemAktivitasBinding
import java.text.SimpleDateFormat
import java.util.*

class AktivitasAdapter(private var list: List<Any>) : RecyclerView.Adapter<AktivitasAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAktivitasBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAktivitasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            when (item) {
                is TugasModel -> {
                    val color = "#42A5F5" // Biru untuk Tugas
                    viewIndicator.setBackgroundColor(Color.parseColor(color))
                    tvLabel.text = "Tugas Selesai"
                    tvLabel.setTextColor(Color.parseColor(color))
                    tvDesc.text = item.tugas
                    tvLocation.text = "Ruangan: ${item.ruangan}, ${item.villa_nama}"

                    // Mengonversi Long timestamp ke format tanggal
                    tvDateTime.text = formatTimestamp(item.completed_at)

                    tvDateTime.setTextColor(Color.parseColor(color))
                }
                is LaporanModel -> {
                    val color = "#C64756" // Merah untuk Laporan
                    viewIndicator.setBackgroundColor(Color.parseColor(color))
                    tvLabel.text = "Laporan ${item.tipe_laporan.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
                    tvLabel.setTextColor(Color.parseColor(color))
                    tvDesc.text = "${item.nama_barang}: ${item.keterangan}"
                    tvLocation.text = "Area: ${item.area}, ${item.villa_nama}"

                    // Laporan biasanya sudah String (yyyy-MM-dd HH:mm), tampilkan langsung
                    tvDateTime.text = item.waktu_lapor

                    tvDateTime.setTextColor(Color.parseColor(color))
                }
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

    fun updateData(newList: List<Any>) {
        list = newList
        notifyDataSetChanged()
    }
}