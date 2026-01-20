package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.ItemPilihVillaBinding

class PilihVillaAdapter(
    private var listVilla: List<VillaModel> = emptyList(),
    private val onVillaClick: (VillaModel) -> Unit
) : RecyclerView.Adapter<PilihVillaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPilihVillaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPilihVillaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val villa = listVilla[position]
        holder.binding.apply {
            tvNamaVilla.text = villa.nama
            tvJumlahRuangan.text = "${villa.areas.size} Ruangan" // Pakai areas

            com.bumptech.glide.Glide.with(ivVilla.context)
                .load(villa.foto)
                .placeholder(R.drawable.ic_launcher_background)
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