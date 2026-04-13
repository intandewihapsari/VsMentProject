package com.indri.vsmentproject.ui.manager.task

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding

class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        items = newList
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemProgresVillaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: VillaTugasGroup) {
            binding.apply {

                // 🏠 Nama Villa
                tvNamaVilla.text = group.namaVilla.ifEmpty { "-" }

                // 🔥 HITUNG REALTIME DARI listTugas
                val total = group.listTugas.size

                val selesai = group.listTugas.count {
                    it.status.equals("selesai", true)
                }

                val pending = group.listTugas.count {
                    it.status.equals("pending", true)
                }

                tvSelesaiCount.text = "Selesai : $selesai"
                tvPendingCount.text = "Pending : $pending"

                // 🔥 PROGRESS AUTO
                val progressInt = if (total > 0) {
                    (selesai * 100) / total
                } else 0

                pbProgres.progress = progressInt
                tvPersen.text = "$progressInt%"

                // 🎨 WARNA PROGRESS (BIAR HIDUP)
                val drawable = DrawableCompat.wrap(pbProgres.progressDrawable)
                when {
                    progressInt >= 80 -> DrawableCompat.setTint(drawable, Color.parseColor("#4CAF50")) // hijau
                    progressInt >= 50 -> DrawableCompat.setTint(drawable, Color.parseColor("#FFC107")) // kuning
                    else -> DrawableCompat.setTint(drawable, Color.parseColor("#F44336")) // merah
                }

                // 🧑 FOTO STAFF
                val tugasPertama = group.listTugas.firstOrNull()

                if (!tugasPertama?.worker_photo.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(tugasPertama?.worker_photo)
                        .circleCrop()
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivStaffProfile)
                } else {
                    ivStaffProfile.setImageResource(
                        android.R.drawable.ic_menu_report_image
                    )
                }
            }
        }
    }
}