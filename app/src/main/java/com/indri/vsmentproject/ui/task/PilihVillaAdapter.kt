package com.indri.vsmentproject.ui.task

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.ItemPilihVillaBinding

class PilihVillaAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PilihVillaAdapter.ViewHolder>() {

    private var listVilla = listOf<VillaModel>()

    fun updateData(newList: List<VillaModel>) {
        this.listVilla = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPilihVillaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val villa = listVilla[position]
        holder.bind(villa) // Panggil fungsi bind di sini saja
    }

    override fun getItemCount() = listVilla.size

    inner class ViewHolder(val binding: ItemPilihVillaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(villa: VillaModel) {
            // Gunakan villa.nama (sesuai model baru)
            binding.tvNamaVilla.text = villa.nama

            // Karena di JSON villa_list tidak ada "jumlahRuangan",
            // kita tampilkan jumlah "area" saja sebagai pengganti
            binding.tvJumlahRuangan.text = "${villa.area.size} Area"

            Glide.with(binding.root.context)
                .load(villa.foto) // Gunakan villa.foto (sesuai model baru)
                .placeholder(R.drawable.ic_menu_gallery)
                .into(binding.ivVilla)

            binding.root.setOnClickListener {
                onItemClick(villa.nama)
            }
        }
    }
}