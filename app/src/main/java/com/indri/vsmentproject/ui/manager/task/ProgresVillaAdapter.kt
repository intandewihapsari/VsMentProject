package com.indri.vsmentproject.ui.manager.task

import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
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
                tvNamaVilla.text = group.namaVilla

                // 📊 Hitung ulang biar aman
                val total = group.listTugas.size
                val selesai = group.listTugas.count {
                    it.status.equals("selesai", true)
                }
                val pending = total - selesai

                tvSelesaiCount.text = "Selesai : $selesai"
                tvPendingCount.text = "Pending : $pending"

                // 🔥 HITUNG PERSENTASE LANGSUNG (ANTI BUG)
                val progressInt = if (total > 0) {
                    (selesai * 100) / total
                } else 0

                tvPersen.text = "$progressInt%"

                // 💣 RESET DULU (WAJIB DI RECYCLER)
                pbProgres.progress = 0
                pbProgres.max = 100

                // 💥 SET PROGRESS
                pbProgres.post {
                    pbProgres.progress = progressInt
                }

                // 🎨 WARNA (HANYA PROGRESS LAYER)
                val drawable = pbProgres.progressDrawable
                if (drawable is LayerDrawable) {
                    val progressLayer =
                        drawable.findDrawableByLayerId(android.R.id.progress)

                    val context = binding.root.context

                    when {
                        progressInt >= 80 -> {
                            DrawableCompat.setTint(
                                progressLayer,
                                ContextCompat.getColor(context, R.color.myGreenDark)
                            )
                        }

                        progressInt >= 50 -> {
                            DrawableCompat.setTint(
                                progressLayer,
                                ContextCompat.getColor(context, R.color.myOrangeDark)
                            )
                        }

                        else -> {
                            DrawableCompat.setTint(
                                progressLayer,
                                ContextCompat.getColor(context, R.color.myRedDark)
                            )
                        }
                    }
                }
            }
        }
    }
}