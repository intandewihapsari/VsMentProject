package com.indri.vsmentproject.ui.tugas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.ItemPilihVillaBinding

class PilihVillaAdapter(
    private var listVilla: List<VillaModel> = emptyList(),
    private val onVillaClick: (VillaModel) -> Unit
) : RecyclerView.Adapter<PilihVillaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPilihVillaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPilihVillaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val villa = listVilla[position]
        holder.binding.apply {
            tvNamaVilla.text = villa.nama
            tvJumlahRuangan.text = "${villa.area.size} Ruangan"

            // LOAD GAMBAR DENGAN GLIDE
            Glide.with(ivVilla.context)
                .load(villa.foto) // Sesuai field "foto" di VillaModel kamu
                .placeholder(android.R.drawable.ic_menu_gallery) // Sambil nunggu
                .error(android.R.drawable.stat_notify_error) // Jika URL salah
                .centerCrop()
                .into(ivVilla)

            root.setOnClickListener { onVillaClick(villa) }
        }
    }

    override fun getItemCount(): Int = listVilla.size

    fun updateData(newList: List<VillaModel>) {
        listVilla = newList
        notifyDataSetChanged()
    }
}