package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.WaktuContainer

class WaktuContainerAdapter : RecyclerView.Adapter<WaktuContainerAdapter.ViewHolder>() {

    private var items = listOf<WaktuContainer>()

    fun submitList(newList: List<WaktuContainer>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Gunakan layout item_container_waktu (yang ada CardView besar & RV di dalamnya)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_waktu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.tvHeader.text = data.kategoriWaktu

        // Pasang Adapter Dalam
        val innerAdapter = InnerVillaAdapter()
        holder.rvInner.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = innerAdapter
            // Agar lancar di dalam scroll
            isNestedScrollingEnabled = false
        }
        innerAdapter.setData(data.listVilla)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader = view.findViewById<TextView>(R.id.tvHeaderWaktu)
        val rvInner = view.findViewById<RecyclerView>(R.id.rvInnerVilla)
    }
}