package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.DeadlineGroup
import com.indri.vsmentproject.databinding.ItemDeadlineBinding

class DeadlineAdapter(
    private val list: List<DeadlineGroup>
) : RecyclerView.Adapter<DeadlineAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDeadlineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeadlineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        val selesai = item.listTugas.count { it.status == "selesai" }
        val total = item.listTugas.size

        holder.binding.tvDeadline.text = item.deadline
        holder.binding.tvSummary.text = "$selesai / $total selesai"

        holder.binding.root.setOnClickListener {
            showDetailDialog(holder.itemView.context, item)
        }
    }

    private fun showDetailDialog(context: android.content.Context, item: DeadlineGroup) {

        val dialog = Dialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_detail_tanggal, null)

        dialog.setContentView(view)

        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val rvTugas = view.findViewById<RecyclerView>(R.id.rvTugas)
        val rvFoto = view.findViewById<RecyclerView>(R.id.rvFoto)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyFoto)

        tvTanggal.text = item.deadline

        rvTugas.layoutManager = LinearLayoutManager(context)
        rvTugas.adapter = TugasSimpleAdapter(item.listTugas)

        if (item.foto.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            rvFoto.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            rvFoto.visibility = android.view.View.VISIBLE

            rvFoto.layoutManager = GridLayoutManager(context, 3)
            val fotoAdapter = FotoAdapter()
            rvFoto.adapter = fotoAdapter
            fotoAdapter.setData(item.foto)
        }

        dialog.show()
    }
}