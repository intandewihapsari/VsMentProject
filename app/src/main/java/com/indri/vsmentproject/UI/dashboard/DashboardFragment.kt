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
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        // Data Dashboard Utama
        viewModel.notifikasiUrgent.observe(viewLifecycleOwner) { listNotif ->
            val dashboardItems = listOf(
                DashboardItem.NotifikasiUrgent(listNotif),
                DashboardItem.AnalisisCepat(emptyList()),
                DashboardItem.AksiCepat,
                DashboardItem.Inventaris,
                DashboardItem.TugasPending
            )
            dashboardAdapter.update(dashboardItems)
        }

        // Data Villa ke Spinner
        viewModel.getVillaList().observe(viewLifecycleOwner) { villaList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, villaList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutFormTambahTugas.spinnerVilla.adapter = adapter
        }

        // Data Staff ke Spinner
        viewModel.getStaffList().observe(viewLifecycleOwner) { staffList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, staffList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutFormTambahTugas.spinnerStaff.adapter = adapter
        }
    }

    private fun setupFormListeners() {
        val formTugas = binding.layoutFormTambahTugas

        formTugas.btnSimpanTugas.setOnClickListener {
            // 1. Ambil inputan dari View Binding
            val namaTugas = formTugas.etNamaTugas.text.toString().trim()
            val deskripsi = formTugas.etDeskripsiTugas.text.toString().trim()
            val tenggat = formTugas.tvDeadlineDate.text.toString()

            // 2. Ambil Pilihan dari Spinner & RadioButton
            val villaTerpilih = formTugas.spinnerVilla.selectedItem.toString()
            val staffTerpilih = formTugas.spinnerStaff.selectedItem.toString()
            val kategori = getCheckedCategory() // Fungsi yang kita buat sebelumnya

            // 3. Validasi
            if (namaTugas.isEmpty() || kategori.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi Nama & Kategori!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Bungkus data ke dalam Map (Sesuai struktur JSON baru)
            val taskData = mapOf(
                "tugas" to namaTugas,
                "keterangan" to deskripsi,
                "kategori" to kategori,
                "staff_nama" to staffTerpilih,
                "villa_target" to villaTerpilih,
                "status" to "pending",
                "waktu_tenggat" to tenggat
            )

            // 5. Eksekusi Simpan ke Firebase melalui ViewModel
            viewModel.simpanTugas(taskData, villaTerpilih) {
                Toast.makeText(requireContext(), "Tugas Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                resetFormTugas()
                switchView(showDashboard = true)
            }
        }
    }

    // Fungsi khusus untuk mencari RadioButton yang dicentang di dalam Binding
    private fun getCheckedCategory(): String {
        val rg = binding.layoutFormTambahTugas.rgKategori
        val id = rg.checkedRadioButtonId
        return if (id != -1) {
            // Kita cari view-nya melalui root form agar lebih akurat
            val rb = binding.layoutFormTambahTugas.root.findViewById<RadioButton>(id)
            rb?.text?.toString() ?: ""
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}