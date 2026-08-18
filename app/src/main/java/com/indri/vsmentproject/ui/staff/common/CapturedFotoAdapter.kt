package com.indri.vsmentproject.ui.staff.common

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.databinding.ItemCapturedFotoBinding
import com.bumptech.glide.Glide

class CapturedFotoAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<CapturedFotoAdapter.ViewHolder>() {

    private val items = mutableListOf<Uri>()

    fun updateData(newList: List<Uri>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemCapturedFotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCapturedFotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = items[position]
        
        // Gunakan Glide agar pemrosesan gambar kamera yang besar jadi ringan
        Glide.with(holder.itemView.context)
            .load(uri)
            .centerCrop()
            .into(holder.binding.ivCaptured)

        holder.binding.btnRemove.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = items.size
}
