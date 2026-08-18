package com.indri.vsmentproject.ui.manager.report

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.databinding.ActivityDetailLaporanBinding
import com.indri.vsmentproject.ui.manager.task.progressVilla.FotoAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DetailLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLaporanBinding
    private val viewModel: LaporanViewModel by viewModels()
    private lateinit var fotoAdapter: FotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<LaporanModel>("DATA_LAPORAN")
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()

        data?.let { laporan ->
            setupUI(laporan)

            binding.btnSimpanCatatan.setOnClickListener {
                val catatan = binding.etCatatanManager.text.toString().trim()
                if (managerUid.isNotEmpty()) {
                    viewModel.updateCatatanManager(managerUid, laporan.id, catatan) { sukses ->
                        if (sukses) {
                            laporan.catatan_manager = catatan 
                            
                            // Jalankan proses PDF di background agar tidak macet saat download gambar
                            lifecycleScope.launch(Dispatchers.IO) {
                                generatePdfReport(laporan)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@DetailLaporanActivity, "Catatan disimpan & PDF diunduh!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            hideKeyboard()
                        }
                    }
                }
            }

            if (laporan.status.lowercase() == "pending" || laporan.status.lowercase() == "proses") {
                binding.btnSelesaikanLaporan.visibility = View.VISIBLE
                binding.btnSelesaikanLaporan.setOnClickListener {
                    val catatan = binding.etCatatanManager.text.toString().trim()

                    if (managerUid.isNotEmpty()) {
                        viewModel.updateStatusLaporan(managerUid, laporan.id, "selesai") { sukses ->
                            if (sukses) {
                                viewModel.updateCatatanManager(managerUid, laporan.id, catatan) {
                                    Toast.makeText(this, "Laporan Selesai!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        }
                    }
                }
            } else {
                binding.btnSelesaikanLaporan.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        fotoAdapter = FotoAdapter()
        binding.rvBuktiFoto.apply {
            layoutManager = LinearLayoutManager(this@DetailLaporanActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = fotoAdapter
        }
    }

    private fun setupUI(it: LaporanModel) {
        val (color, label) = when (it.tipe_laporan.lowercase()) {
            "rusak" -> Pair(ContextCompat.getColor(this, R.color.myRedDark), "KERUSAKAN")
            "hilang" -> Pair(ContextCompat.getColor(this, R.color.myOrangeDark), "HILANG")
            "habis" -> Pair(ContextCompat.getColor(this, R.color.myGreen), "STOK HABIS")
            else -> Pair(Color.GRAY, "LAINNYA")
        }
        binding.tvTipeBesar.text = label
        binding.tvTipeBesar.setTextColor(color)
        binding.cardTipe.setStrokeColor(ColorStateList.valueOf(color))

        binding.tvBarang.text = it.nama_barang
        binding.tvStatusDetail.text = it.status.uppercase()
        binding.tvVilla.text = "Villa: ${it.villa_nama}"
        binding.tvArea.text = "Area: ${it.area}"
        binding.tvStaff.text = "Staff: ${it.staff_nama}"
        binding.tvWaktu.text = "Dilaporkan: ${it.waktu_lapor}"
        binding.tvDeskripsi.text = it.deskripsi
        binding.etCatatanManager.setText(it.catatan_manager)

        val listFoto = if (it.bukti_foto.isNotEmpty()) it.bukti_foto else listOf(it.foto_url)
        fotoAdapter.setData(listFoto.filter { it.isNotEmpty() })
    }

    private suspend fun generatePdfReport(laporan: LaporanModel) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.BLACK
        }
        val valuePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = Color.BLACK
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader(cv: Canvas) {
            val hTitlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20f
                color = Color.BLACK
            }
            val hSubPaint = Paint().apply {
                textSize = 12f
                color = Color.DKGRAY
            }
            cv.drawText("LAPORAN OPERASIONAL VILLA", 50f, 50f, hTitlePaint)
            cv.drawText("VsMent - Villa Management System", 50f, 70f, hSubPaint)
            cv.drawLine(50f, 85f, 545f, 85f, Paint().apply { strokeWidth = 2f; color = Color.BLACK })
        }

        drawHeader(canvas)

        var yPos = 120f
        val startX = 50f
        val valueX = 160f // Posisi setelah titik dua agar sejajar

        fun checkNewPage() {
            if (yPos > 780f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawHeader(canvas)
                yPos = 120f
            }
        }

        fun drawField(label: String, value: String) {
            canvas.drawText(label, startX, yPos, labelPaint)
            canvas.drawText(": $value", valueX, yPos, valuePaint)
            yPos += 25f
            checkNewPage()
        }

        // 1. Basic Info
        drawField("ID Laporan", laporan.id)
        drawField("Nama Villa", laporan.villa_nama)
        drawField("Area / Lokasi", laporan.area)
        drawField("Tipe Laporan", laporan.tipe_laporan.uppercase())
        drawField("Nama Barang", laporan.nama_barang)
        drawField("Dilaporkan Oleh", laporan.staff_nama)
        drawField("Waktu Lapor", laporan.waktu_lapor)
        drawField("Status", laporan.status.uppercase())

        yPos += 10f

        // 2. Multiline Description
        canvas.drawText("Deskripsi Kendala:", startX, yPos, labelPaint)
        yPos += 20f
        yPos = drawMultilineText(laporan.deskripsi, startX + 10, yPos, 480, canvas, valuePaint)
        yPos += 20f
        checkNewPage()

        // 3. Grid Photos (Semua Foto)
        val allPhotos = if (laporan.bukti_foto.isNotEmpty()) laporan.bukti_foto else listOf(laporan.foto_url).filter { it.isNotEmpty() }
        if (allPhotos.isNotEmpty()) {
            canvas.drawText("Foto Bukti Lapangan:", startX, yPos, labelPaint)
            yPos += 25f
            checkNewPage()

            val photoWidth = 230f
            val spacing = 20f
            var xPos = startX

            for ((index, photoUrl) in allPhotos.withIndex()) {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        Glide.with(this@DetailLaporanActivity).asBitmap().load(photoUrl).submit().get()
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
                            yPos = 120f
                            xPos = startX
                        }

                        val rect = RectF(xPos, yPos, xPos + photoWidth, yPos + h)
                        canvas.drawBitmap(bitmap, null, rect, null)

                        if (index % 2 == 0) {
                            xPos = startX + photoWidth + spacing
                        } else {
                            xPos = startX
                            yPos += h + spacing
                        }
                    }
                } catch (e: Exception) {
                    canvas.drawText("[Gagal memuat gambar]", xPos, yPos, valuePaint)
                }
            }
            if (allPhotos.size % 2 != 0) yPos += 180f // Jeda jika ganjil
            yPos += 20f
            checkNewPage()
        }

        // 4. Manager Notes
        canvas.drawText("Catatan Manager:", startX, yPos, labelPaint.apply { color = Color.parseColor("#C2185B") })
        yPos += 20f
        val managerNote = if (laporan.catatan_manager.isEmpty()) "-" else laporan.catatan_manager
        yPos = drawMultilineText(managerNote, startX + 10, yPos, 480, canvas, valuePaint)

        // Footer di halaman terakhir
        val footerPaint = Paint().apply { textSize = 10f; color = Color.GRAY }
        canvas.drawText("Halaman $pageNumber - VsMent Auto-Generated", 50f, 820f, footerPaint)

        pdfDocument.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Report_${laporan.id}_$timeStamp.pdf"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetailLaporanActivity, "PDF Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawMultilineText(text: String, x: Float, y: Float, maxWidth: Int, canvas: Canvas, paint: Paint): Float {
        var currentY = y
        val words = text.split(" ")
        var line = StringBuilder()

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "${line} $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                canvas.drawText(line.toString(), x, currentY, paint)
                line = StringBuilder(word)
                currentY += 20f
            } else {
                line = StringBuilder(testLine)
            }
        }
        canvas.drawText(line.toString(), x, currentY, paint)
        return currentY + 20f
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
