package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.databinding.ItemProgresVillaBinding

class ProgresVillaAdapter : RecyclerView.Adapter<ProgresVillaAdapter.ViewHolder>() {

    private var items = listOf<VillaTugasGroup>()

    fun setList(newList: List<VillaTugasGroup>) {
        this.items = newList
        notifyDataSetChanged()
    }

    // Fungsi helper kalau kamu butuh ambil data tugas secara flat
    fun getTaskAt(position: Int): TugasModel? {
        val flatList = items.flatMap { it.listTugas }
        return if (position in flatList.indices) flatList[position] else null
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
            // 1. Set Nama Villa
            binding.tvNamaVilla.text = group.namaVilla

            // 2. Hitung Statistik Tugas
            val total = group.listTugas.size
            val selesai = group.listTugas.count { it.status.equals("selesai", true) || it.status.equals("done", true) }
            val pending = total - selesai

            binding.tvSelesaiCount.text = "Selesai : $selesai"
            binding.tvPendingCount.text = "Pending : $pending"

            // 3. Set Progress Bar & Persentase
            val progressInt = if (total > 0) (selesai * 100) / total else 0
            binding.tvPersen.text = "$progressInt%"

            binding.pbProgres.max = 100
            // Menggunakan setProgress dengan animasi (jika API 24+) atau biasa
            binding.pbProgres.post {
                binding.pbProgres.setProgress(progressInt, true)
            }

            // 4. Load Foto Profil Staff (Ambil dari staff_id tugas pertama di villa tersebut)
            val staffId = group.listTugas.firstOrNull { !it.staff_id.isNullOrEmpty() }?.staff_id

            if (!staffId.isNullOrEmpty()) {
                loadStaffPhoto(staffId)
            } else {
                // Default icon kalau tidak ada staff_id
                binding.ivStaffProfile.setImageResource(R.drawable.ic_profile_deffault) // Pastikan ada drawable ini atau ganti ic_menu_report_image
            }
        }

        private fun loadStaffPhoto(staffId: String) {
            // Pastikan path "users/staffs" sesuai dengan struktur Firebase kamu
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child("staffs") // Bisa digabung .getReference("users/staffs")
                .child(staffId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val photo = snapshot.child("foto_profil").getValue(String::class.java)

                        if (!photo.isNullOrEmpty()) {
                            Glide.with(binding.root.context)
                                .load(photo)
                                .circleCrop() // Biar foto profilnya bulat cantik
                                .placeholder(R.drawable.ic_profile_deffault)
                                .error(R.drawable.ic_profile_deffault)
                                .into(binding.ivStaffProfile)
                        }
                    }
                }
                .addOnFailureListener {
                    binding.ivStaffProfile.setImageResource(R.drawable.ic_profile)
                }

        }

    }
}