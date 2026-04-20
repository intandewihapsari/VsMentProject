package com.indri.vsmentproject.ui.manager.task

import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding
class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()


    fun setList(newList: List<VillaTugasGroup>) {
        items = newList
        notifyDataSetChanged()
    }
    fun getTaskAt(position: Int): TugasModel {
        val flatList = items.flatMap { it.listTugas }
        return flatList[position]
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

    inner class ViewHolder(private val binding: ItemProgresVillaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: VillaTugasGroup) {

            binding.tvNamaVilla.text = group.namaVilla

            val total = group.listTugas.size
            val selesai = group.listTugas.count { it.status.equals("selesai", true) }
            val pending = total - selesai

            binding.tvSelesaiCount.text = "Selesai : $selesai"
            binding.tvPendingCount.text = "Pending : $pending"

            val progressInt = if (total > 0) (selesai * 100) / total else 0

            binding.tvPersen.text = "$progressInt%"

            binding.pbProgres.progress = 0
            binding.pbProgres.max = 100
            binding.pbProgres.post {
                binding.pbProgres.progress = progressInt
            }

            // 🔥 AMBIL STAFF ID DARI TASK PERTAMA
            val staffId = group.listTugas.firstOrNull()?.staff_id

            if (!staffId.isNullOrEmpty()) {
                loadStaffPhoto(staffId)
            } else {
                binding.ivStaffProfile.setImageResource(
                    android.R.drawable.ic_menu_report_image
                )
            }
        }

        private fun loadStaffPhoto(staffId: String) {

            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users/staffs")
                .child(staffId)
                .get()
                .addOnSuccessListener { snapshot ->

                    val photo = snapshot.child("foto_profil")
                        .getValue(String::class.java)

                    com.bumptech.glide.Glide.with(binding.root.context)
                        .load(photo)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivStaffProfile)
                }
        }
    }
}