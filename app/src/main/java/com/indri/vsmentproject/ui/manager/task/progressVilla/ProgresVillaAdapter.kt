package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.DeadlineGroup
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
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
            // PERBAIKAN: Gunakan pembanding ignore case agar status tetap presisi
            val selesai = group.listTugas.count { it.status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true) }

            binding.tvNamaVilla.text = group.namaVilla
            binding.tvSelesaiCount.text = "Selesai : $selesai"
            binding.tvPendingCount.text = "Pending : ${total - selesai}"

            // Pengambilan foto PIC profil staff (menggunakan field staff_foto dari TugasModel kamu)
            val foto = group.listTugas.firstOrNull()?.foto_staff

            val progress = if (total > 0) (selesai * 100) / total else 0
            binding.tvPersen.text = "$progress%"
            binding.pbProgres.progress = progress

            // VALIDASI STATUS OPERASIONAL
            val semuaSelesai = group.listTugas.all { it.status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true) }
            
            // Agregasi seluruh foto secara defensif
            val seluruhFoto = mutableListOf<String>()
            group.listTugas.forEach { tugas ->
                tugas.bukti_foto?.let { seluruhFoto.addAll(it) }
                if (tugas.foto_tugas.isNotEmpty()) seluruhFoto.add(tugas.foto_tugas)
            }
            val finalPhotos = seluruhFoto.filter { it.isNotEmpty() }.distinct()
            val adaFoto = finalPhotos.isNotEmpty()
            
            // LOG DEBUG: Bantu cek apakah data terbaca
            android.util.Log.d("PROGRES_SYNC", "Villa: ${group.namaVilla} | Foto Ditemukan: ${finalPhotos.size}")

            val mContext = binding.root.context

            // SINKRONISASI TEKS & WARNA LATAR BELAKANG STATUS
            when {
                !semuaSelesai -> {
                    binding.tvStatusValidasi.text = "⏳ Masih Proses"
                    binding.tvStatusValidasi.setBackgroundColor(ContextCompat.getColor(mContext, R.color.myRedDark))
                }
                semuaSelesai && !adaFoto -> {
                    binding.tvStatusValidasi.text = "📷 Belum Ada Bukti"
                    binding.tvStatusValidasi.setBackgroundColor(ContextCompat.getColor(mContext, R.color.myOrangeDark))
                }
                else -> {
                    binding.tvStatusValidasi.text = "✅ Sudah Upload"
                    binding.tvStatusValidasi.setBackgroundColor(ContextCompat.getColor(mContext, R.color.myGreen))
                }
            }
            binding.tvStatusValidasi.setTextColor(android.graphics.Color.WHITE)

            if (!foto.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(foto)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivStaffProfile)
            } else {
                binding.ivStaffProfile.setImageResource(R.drawable.ic_profile)
            }

            binding.root.setOnClickListener {
                showDialog(bindingAdapterPosition)
            }
        }

        private fun showDialog(position: Int) {
            val context = binding.root.context
            val group = items[position]

            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_detail_villa, null)
            dialog.setContentView(view)

            val tvNama = view.findViewById<TextView>(R.id.tvNamaVilla)
            val rvDeadline = view.findViewById<RecyclerView>(R.id.rvDeadline)
            val btnClose = view.findViewById<ImageView>(R.id.btnClose)

            tvNama.text = group.namaVilla

            // GROUP BY DEADLINE TANGGAL
            val grouped = group.listTugas.groupBy { it.deadline }
            val result = grouped.map { (tanggal, tugasList) ->
                val photosForDate = mutableListOf<String>()
                tugasList.forEach { t ->
                    t.bukti_foto?.let { photosForDate.addAll(it) }
                    if (t.foto_tugas.isNotEmpty()) photosForDate.add(t.foto_tugas)
                }

                DeadlineGroup(
                    deadline = tanggal,
                    listTugas = tugasList,
                    foto = photosForDate.filter { it.isNotEmpty() }.distinct()
                )
            }

            btnClose.setOnClickListener { dialog.dismiss() }

            rvDeadline.layoutManager = LinearLayoutManager(context)
            rvDeadline.adapter = DeadlineAdapter(result)

            dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }
}