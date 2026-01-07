package com.indri.vsmentproject.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.UI.dashboard.DashboardItem
import com.indri.vsmentproject.UI.dashboard.DashboardViewModel
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupFormListeners()
    }

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            onTambahTugasClick = { switchView(showTugas = true) },
            onKirimNotifClick = { switchView(showNotif = true) }
        )

        binding.rvDashboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dashboardAdapter
        }
    }

    private fun setupObservers() {
        // 1. Pemicu awal untuk menghitung data dari Firebase
        viewModel.loadInventarisSummary()

        // 2. Observer Utama: Kita gabungkan Notifikasi dan Inventaris
        viewModel.notifikasiUrgent.observe(viewLifecycleOwner) { listNotif ->
            viewModel.inventarisData.observe(viewLifecycleOwner) { dataInv ->
                viewModel.listTugasPending.observe(viewLifecycleOwner) { listTugas ->
                    // Buat daftar item untuk ditampilkan di RecyclerView
                    val dashboardItems = listOf(
                        DashboardItem.NotifikasiUrgent(listNotif),
                        DashboardItem.AnalisisCepat(emptyList()),
                        DashboardItem.AksiCepat,
                        DashboardItem.Inventaris(dataInv), // Data hasil hitung masuk ke sini
                        DashboardItem.TugasPending(listTugas)
                    )

                    // Kirim ke adapter
                    dashboardAdapter.update(dashboardItems)
                }
            }
        }

        // 3. Observer Data Villa (Tetap di luar observer pertama)
        viewModel.getVillaList().observe(viewLifecycleOwner) { villaList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, villaList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutFormTambahTugas.spinnerVilla.adapter = adapter
            binding.layoutFormKirimNotifikasi.spinnerVillaNotif.adapter = adapter
        }

        // 4. Observer Data Staff
        viewModel.getStaffList().observe(viewLifecycleOwner) { staffList ->
            val adapterTugas = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, staffList)
            binding.layoutFormTambahTugas.spinnerStaff.adapter = adapterTugas

            val listTargetNotif = mutableListOf("Semua Staff")
            listTargetNotif.addAll(staffList)
            val adapterNotif = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listTargetNotif)
            binding.layoutFormKirimNotifikasi.spinnerTargetNotif.adapter = adapterNotif
        }
    }
    private fun setupFormListeners() {
        val formTugas = binding.layoutFormTambahTugas
        val formNotif = binding.layoutFormKirimNotifikasi

        // --- LOGIKA FORM TUGAS ---
        formTugas.btnPilihTanggal.setOnClickListener { showDatePicker() }

        formTugas.btnSimpanTugas.setOnClickListener {
            val namaTugas = formTugas.etNamaTugas.text.toString().trim()
            val kategori = getCheckedCategory()
            val villa = formTugas.spinnerVilla.selectedItem?.toString() ?: ""

            if (namaTugas.isEmpty() || kategori.isEmpty() || villa.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi data tugas!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val taskData = mapOf(
                "tugas" to namaTugas,
                "keterangan" to formTugas.etDeskripsiTugas.text.toString().trim(),
                "kategori" to kategori,
                "staff_nama" to formTugas.spinnerStaff.selectedItem.toString(),
                "villa_target" to villa,
                "status" to "pending",
                "waktu_tenggat" to formTugas.tvDeadlineDate.text.toString()
            )

            viewModel.simpanTugas(taskData, villa) {
                Toast.makeText(requireContext(), "Tugas Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                resetFormTugas()
                switchView(showDashboard = true)
            }
        }

        // --- LOGIKA FORM NOTIFIKASI ---
        formNotif.btnKirimNotifFirebase.setOnClickListener {
            val judul = formNotif.etJudulNotif.text.toString().trim()
            val pesan = formNotif.etPesanNotif.text.toString().trim()
            val target = formNotif.spinnerTargetNotif.selectedItem?.toString() ?: ""
            val villa = formNotif.spinnerVillaNotif.selectedItem?.toString() ?: ""

            // CEK APAKAH URGENT ATAU INFO
            val isUrgent = formNotif.switchUrgent.isChecked
            val tipeNotif = if (isUrgent) "urgent" else "info"

            if (judul.isEmpty() || pesan.isEmpty()) {
                Toast.makeText(requireContext(), "Judul dan Pesan wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notifData = mapOf(
                "judul" to judul,
                "pesan" to pesan,
                "ditujukan_ke" to target,
                "villa_terkait" to villa,
                "waktu" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                "status_baca" to false,
                "tipe" to tipeNotif // Nilainya sekarang dinamis: "urgent" atau "info"
            )

            viewModel.kirimNotifikasi(notifData) {
                val pesanSukses = if (isUrgent) "Notifikasi DARURAT Terkirim!" else "Notifikasi Terkirim"
                Toast.makeText(requireContext(), pesanSukses, Toast.LENGTH_SHORT).show()

                resetFormNotif()
                switchView(showDashboard = true)
            }
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = "$day/${month + 1}/$year"
            binding.layoutFormTambahTugas.tvDeadlineDate.text = date
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getCheckedCategory(): String {
        val rg = binding.layoutFormTambahTugas.rgKategori
        val id = rg.checkedRadioButtonId
        return if (id != -1) {
            binding.layoutFormTambahTugas.root.findViewById<RadioButton>(id)?.text?.toString() ?: ""
        } else ""
    }

    private fun switchView(showDashboard: Boolean = false, showTugas: Boolean = false, showNotif: Boolean = false) {
        binding.rvDashboard.visibility = if (showDashboard) View.VISIBLE else View.GONE
        binding.layoutFormTambahTugas.root.visibility = if (showTugas) View.VISIBLE else View.GONE
        binding.layoutFormKirimNotifikasi.root.visibility = if (showNotif) View.VISIBLE else View.GONE
    }

    private fun resetFormTugas() {
        binding.layoutFormTambahTugas.apply {
            etNamaTugas.text.clear()
            etDeskripsiTugas.text.clear()
            rgKategori.clearCheck()
            tvDeadlineDate.text = "Pilih Tanggal"
        }
    }

    private fun resetFormNotif() {
        binding.layoutFormKirimNotifikasi.apply {
            etJudulNotif.text.clear()
            etPesanNotif.text.clear()
            switchUrgent.isChecked = false // Reset switch
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}