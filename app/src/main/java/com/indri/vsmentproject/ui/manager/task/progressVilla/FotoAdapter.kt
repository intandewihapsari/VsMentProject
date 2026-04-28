package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.databinding.ItemFotoBinding

class FotoAdapter : RecyclerView.Adapter<FotoAdapter.ViewHolder>() {

    private var items = listOf<String>()

    fun setData(data: List<String>) {
        items = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemFotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(items[position])
            .into(holder.binding.ivFoto)
    }

    override fun getItemCount() = items.size
}