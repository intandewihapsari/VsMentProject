package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.DeadlineGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ItemDeadlineBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DeadlineAdapter(
    private val list: List<DeadlineGroup>
) : RecyclerView.Adapter<DeadlineAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDeadlineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeadlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val selesai = item.listTugas.count { it.status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true) }
        val total = item.listTugas.size

        holder.binding.tvDeadline.text = item.deadline
        holder.binding.tvSummary.text = "$selesai / $total selesai"

        holder.binding.root.setOnClickListener {
            showDetailDialog(holder.itemView.context, item)
        }
    }

    private fun showDetailDialog(context: Context, item: DeadlineGroup) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_detail_tanggal, null)
        dialog.setContentView(view)

        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val rvTugas = view.findViewById<RecyclerView>(R.id.rvTugas)
        val rvFoto = view.findViewById<RecyclerView>(R.id.rvFoto)

        val layoutEmptyFoto = view.findViewById<View>(R.id.layoutEmptyFoto)
        val btnDownload = view.findViewById<MaterialCardView>(R.id.btnDownload)
        val btnClose = view.findViewById<MaterialCardView>(R.id.btnClose)

        tvTanggal.text = item.deadline

        rvTugas.layoutManager = LinearLayoutManager(context)
        rvTugas.adapter = TugasSimpleAdapter(item.listTugas)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnDownload.setOnClickListener {
            // Jalankan ekspor PDF di background
            CoroutineScope(Dispatchers.IO).launch {
                generateProgressPdf(context, item)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF Progres berhasil diunduh!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (item.foto.isEmpty()) {
            layoutEmptyFoto.visibility = View.VISIBLE
            rvFoto.visibility = View.GONE
        } else {
            layoutEmptyFoto.visibility = View.GONE
            rvFoto.visibility = View.VISIBLE

            rvFoto.layoutManager = GridLayoutManager(context, 3)
            val fotoAdapter = FotoAdapter()
            rvFoto.adapter = fotoAdapter
            fotoAdapter.setData(item.foto)
        }

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private suspend fun generateProgressPdf(context: Context, item: DeadlineGroup) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 11f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader(cv: Canvas) {
            cv.drawText("LAPORAN PROGRES TUGAS VILLA", 50f, 50f, titlePaint)
            cv.drawText("VsMent Management System", 50f, 70f, Paint().apply { textSize = 10f; color = Color.GRAY })
            cv.drawLine(50f, 80f, 545f, 80f, Paint().apply { strokeWidth = 1f; color = Color.BLACK })
        }

        drawHeader(canvas)

        var yPos = 110f

        // Info Summary
        canvas.drawText("Villa:", 50f, yPos, labelPaint)
        canvas.drawText(item.listTugas.firstOrNull()?.villa_nama ?: "-", 150f, yPos, textPaint)
        yPos += 20f
        canvas.drawText("Tanggal Deadline:", 50f, yPos, labelPaint)
        canvas.drawText(item.deadline, 150f, yPos, textPaint)
        yPos += 20f
        val selesaiCount = item.listTugas.count { it.status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true) }
        canvas.drawText("Total Progres:", 50f, yPos, labelPaint)
        canvas.drawText("$selesaiCount / ${item.listTugas.size} Tugas Selesai", 150f, yPos, textPaint)
        yPos += 40f

        // Table Header
        canvas.drawRect(50f, yPos - 15f, 545f, yPos + 5f, Paint().apply { color = Color.LTGRAY })
        canvas.drawText("Tugas", 60f, yPos, labelPaint)
        canvas.drawText("PIC Staff", 250f, yPos, labelPaint)
        canvas.drawText("Status", 450f, yPos, labelPaint)
        yPos += 30f

        // Task List
        item.listTugas.forEach { tugas ->
            canvas.drawText(tugas.tugas, 60f, yPos, textPaint)
            canvas.drawText(tugas.staff_nama, 250f, yPos, textPaint)
            
            val statusPaint = Paint(textPaint).apply {
                color = if (tugas.status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true)) Color.parseColor("#2E7D32") else Color.RED
            }
            canvas.drawText(tugas.status.uppercase(), 450f, yPos, statusPaint)
            yPos += 25f

            if (yPos > 780f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawHeader(canvas)
                yPos = 110f
            }
        }

        yPos += 30f

        // Photos Evidence
        if (item.foto.isNotEmpty()) {
            canvas.drawText("Bukti Dokumentasi Pekerjaan:", 50f, yPos, labelPaint)
            yPos += 25f

            val photoWidth = 230f
            val spacing = 20f
            var xPos = 50f

            for ((index, photoUrl) in item.foto.withIndex()) {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        Glide.with(context).asBitmap().load(photoUrl).submit().get()
                    }

                    if (bitmap != null) {
                        val scale = photoWidth / bitmap.width
                        val h = bitmap.height * scale

                        if (yPos + h > 780f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            drawHeader(canvas)
                            yPos = 110f
                            xPos = 50f
                        }

                        val rect = RectF(xPos, yPos, xPos + photoWidth, yPos + h)
                        canvas.drawBitmap(bitmap, null, rect, null)

                        if (index % 2 == 0) {
                            xPos = 50f + photoWidth + spacing
                        } else {
                            xPos = 50f
                            yPos += h + spacing
                        }
                    }
                } catch (e: Exception) {
                    canvas.drawText("[Gambar gagal dimuat]", xPos, yPos, textPaint)
                }
            }
        }

        canvas.drawText("Halaman $pageNumber - VsMent Progres Report", 50f, 820f, Paint().apply { textSize = 9f; color = Color.GRAY })
        pdfDocument.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Progres_${item.listTugas.firstOrNull()?.villa_nama ?: "Villa"}_${item.deadline}_$timeStamp.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}
