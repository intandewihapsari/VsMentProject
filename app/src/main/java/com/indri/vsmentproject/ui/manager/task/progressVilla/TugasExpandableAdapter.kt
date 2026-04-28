package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.ItemTugasExpandableBinding

class TugasExpandableAdapter(
    private val list: List<TugasModel>
) : RecyclerView.Adapter<TugasExpandableAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTugasExpandableBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTugasExpandableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tugas = list[position]

        holder.binding.tvNamaTugas.text = tugas.tugas

        // 🔥 kalau tidak ada foto, hide
        if (tugas.bukti_foto.isEmpty()) {
            holder.binding.rvFoto.visibility = View.GONE
        } else {
            holder.binding.rvFoto.visibility = View.VISIBLE

            val fotoAdapter = FotoAdapter()
            holder.binding.rvFoto.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = fotoAdapter
            }

            fotoAdapter.setData(tugas.bukti_foto)
        }

        holder.binding.root.setOnClickListener {
            showDetailDialog(holder.itemView.context, tugas)
        }
    }
    private fun showDetailDialog(context: Context, tugas: TugasModel) {

        val dialog = Dialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_detail_tugas, null)

        dialog.setContentView(view)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvNama = view.findViewById<TextView>(R.id.tvNamaTugas)
        val tvRuangan = view.findViewById<TextView>(R.id.tvRuangan)
        val rvFoto = view.findViewById<RecyclerView>(R.id.rvFoto)

        tvNama.text = tugas.tugas
        tvRuangan.text = tugas.ruangan

        val fotoAdapter = FotoAdapter()

        rvFoto.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = fotoAdapter
        }

        fotoAdapter.setData(tugas.bukti_foto)

        dialog.show()
    }
}