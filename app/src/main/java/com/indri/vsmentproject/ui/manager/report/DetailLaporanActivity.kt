package com.indri.vsmentproject.ui.manager.report

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.databinding.ActivityDetailLaporanBinding

class DetailLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLaporanBinding
    private val viewModel: LaporanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<LaporanModel>("DATA_LAPORAN")
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.btnBack.setOnClickListener { finish() }

        data?.let { laporan ->
            setupUI(laporan)

            // TOMBOL SIMPAN CATATAN
            binding.btnSimpanCatatan.setOnClickListener {
                val catatan = binding.etCatatanManager.text.toString().trim()
                if (managerUid.isNotEmpty()) {
                    viewModel.updateCatatanManager(managerUid, laporan.id, catatan) { sukses ->
                        if (sukses) {
                            Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show()
                            hideKeyboard()
                        }
                    }
                }
            }

            // TOMBOL TANDAI SELESAI
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

        // DETAIL INFO (SUDAH DISINKRONKAN DENGAN NAMA BARU MODEL DATA)
        binding.tvBarang.text = it.nama_barang
        binding.tvStatusDetail.text = it.status.uppercase()
        binding.tvVilla.text = "Villa: ${it.villa_nama}"
        binding.tvArea.text = "Area: ${it.area}"
        binding.tvStaff.text = "Staff: ${it.staff_nama}"
        binding.tvWaktu.text = "Dilaporkan: ${it.waktu_lapor}"
        binding.tvDeskripsi.text = it.deskripsi
        binding.etCatatanManager.setText(it.catatan_manager)

        if (!it.foto_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(it.foto_url)
                .override(800, 800)
                .placeholder(R.drawable.ic_placeholder_img)
                .error(R.drawable.ic_error_img)
                .into(binding.imgBukti)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}