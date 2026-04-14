package com.indri.vsmentproject.ui.manager.masterdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.user.UserModel

class StaffAdapter(
    private val onClick: (UserModel) -> Unit
) : RecyclerView.Adapter<StaffAdapter.ViewHolder>() {

    private var list = listOf<UserModel>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivStaff)
        val nama: TextView = view.findViewById(R.id.tvNama)
        val posisi: TextView = view.findViewById(R.id.tvPosisi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list[position]

        holder.nama.text = data.nama
        holder.posisi.text = data.posisi

        Glide.with(holder.img.context)
            .load(data.foto_profil)
            .placeholder(R.drawable.ic_profile)
            .into(holder.img)

        // 🔥 STATUS FIX
        if (data.status == "aktif") {
            holder.itemView.alpha = 1.0f
        } else {
            holder.itemView.alpha = 0.5f
        }

        holder.itemView.setOnClickListener {
            onClick(data)
        }
    }

    override fun getItemCount() = list.size

    fun submitList(newList: List<UserModel>) {
        list = newList
        notifyDataSetChanged()
    }
}