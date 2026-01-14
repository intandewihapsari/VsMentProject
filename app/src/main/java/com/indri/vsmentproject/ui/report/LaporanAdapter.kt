package com.indri.vsmentproject.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.databinding.ItemTugasPendingListBinding // Gunakan layout yang sudah ada biar cepat

class LaporanAdapter(private val onClick: (LaporanModel) -> Unit) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {
    private var items = listOf<LaporanModel>()

    fun updateList(newList: List<LaporanModel>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTugasPendingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvNamaTugas.text = "${item.nama_barang} (${item.jenis_laporan})"
        holder.binding.tvStatus.text = "Status: ${item.status_laporan.replace("_", " ")}"
        holder.binding.tvPIC.text = "Villa: ${item.villa_nama}"

        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
    class ViewHolder(val binding: ItemTugasPendingListBinding) : RecyclerView.ViewHolder(binding.root)
}