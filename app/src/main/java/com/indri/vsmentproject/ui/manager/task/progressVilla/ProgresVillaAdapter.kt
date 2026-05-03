package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.DeadlineGroup
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding


class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private val items = mutableListOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProgresVillaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: ItemProgresVillaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: VillaTugasGroup) {

            val total = group.listTugas.size
            val selesai = group.listTugas.count { it.status == "selesai" }

            binding.tvNamaVilla.text = group.namaVilla
            binding.tvSelesaiCount.text = "Selesai : $selesai"
            binding.tvPendingCount.text = "Pending : ${total - selesai}"

            val progress = if (total > 0) (selesai * 100) / total else 0
            binding.tvPersen.text = "$progress%"
            binding.pbProgres.progress = progress

            // 🔥 STATUS
            val semuaSelesai = group.listTugas.all { it.status == "selesai" }
            val adaFoto = group.listTugas.any { it.bukti_foto.isNotEmpty() }
            val mContext = binding.root.context
            binding.tvStatusValidasi.text = when {
                !semuaSelesai -> "⏳ Masih Proses"
                semuaSelesai && !adaFoto -> "📷 Belum Ada Bukti"
                else -> "✅ Sudah Upload"
            }

            // 🔥 STATUS (Hardcoded Colors)

            when {
                !semuaSelesai -> {
                    binding.tvStatusValidasi.text = "⏳ Masih Proses"
                    binding.tvStatusValidasi.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(mContext, R.color.myRedDark)
                    )                }
                semuaSelesai && !adaFoto -> {
                    binding.tvStatusValidasi.text = "📷 Belum Ada Bukti"
                    binding.tvStatusValidasi.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(mContext, R.color.myOrangeDark)
                    )                }
                else -> {
                    binding.tvStatusValidasi.text = "✅ Sudah Upload"
                    // Menggunakan warna dari colors.xml
                    binding.tvStatusValidasi.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(mContext, R.color.myGreen)
                    )
                }
            }
// Pastikan warna teks tetap putih agar kontras
            binding.tvStatusValidasi.setTextColor(android.graphics.Color.WHITE)

            // 🔥 CLICK → POPUP
            binding.root.setOnClickListener {
                showDialog(bindingAdapterPosition)
            }
        }

        private fun showDialog(position: Int) {
            val context = binding.root.context
            val group = items[position]

            val dialog = Dialog(context)
            val view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_detail_villa, null)

            dialog.setContentView(view)
            dialog.window?.setLayout(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            val tvNama = view.findViewById<TextView>(R.id.tvNamaVilla)
            val rvDeadline = view.findViewById<RecyclerView>(R.id.rvDeadline)

            tvNama.text = group.namaVilla

            // 🔥 GROUP BY DEADLINE
            val grouped = group.listTugas.groupBy { it.deadline }

            val result = grouped.map { (tanggal, tugasList) ->
                DeadlineGroup(
                    deadline = tanggal,
                    listTugas = tugasList,
                    foto = tugasList.firstOrNull { it.bukti_foto.isNotEmpty() }?.bukti_foto ?: emptyList()
                )
            }
            val btnClose = view.findViewById<ImageView>(R.id.btnClose)

            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            rvDeadline.layoutManager = LinearLayoutManager(context)
            rvDeadline.adapter = DeadlineAdapter(result)

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialog.show()
        }
    }
}