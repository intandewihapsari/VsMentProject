package com.indri.vsmentproject.ui.manager.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R

class AreaAdapter(
    private val list: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AreaAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArea: TextView = view.findViewById(R.id.tvArea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_area, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = list[position]
        holder.tvArea.text = area
        holder.itemView.setOnClickListener {
            onClick(area)
        }
    }
}