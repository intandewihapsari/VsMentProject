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

        // Memicu pengambilan data awal
        viewModel.loadInventarisSummary()
        viewModel.loadTugasPending()
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
            // 1. OBSERVER UTAMA (Otomatis panggil adapter.update saat data apa pun berubah)
            viewModel.combinedDashboardData.observe(viewLifecycleOwner) { items ->
                dashboardAdapter.update(items)
            }

            // 2. OBSERVER SPINNER VILLA
            viewModel.villaList.observe(viewLifecycleOwner) { list ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.layoutFormTambahTugas.spinnerVilla.adapter = adapter
                binding.layoutFormKirimNotifikasi.spinnerVillaNotif.adapter = adapter
            }

            // 3. OBSERVER SPINNER STAFF
            viewModel.staffList.observe(viewLifecycleOwner) { list ->
                val adapterTugas = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
                binding.layoutFormTambahTugas.spinnerStaff.adapter = adapterTugas

                val listNotif = mutableListOf("Semua Staff").apply {
                    addAll(list.filter { it != "Pilih Staff" })
                }
                binding.layoutFormKirimNotifikasi.spinnerTargetNotif.adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listNotif)
            }

    }

    /**
     * Fungsi utama untuk menggabungkan semua data menjadi satu list untuk RecyclerView
     */
    private fun updateDashboard() {
        val listNotif = viewModel.notifikasiUrgent.value ?: emptyList()
        val dataInv = viewModel.inventarisData.value
        val listTugas = viewModel.listTugasPending.value ?: emptyList()

        val dashboardItems = mutableListOf<DashboardItem>()

        // 1. Notifikasi Urgent (Paling Atas)
        dashboardItems.add(DashboardItem.NotifikasiUrgent(listNotif))

        // 2. Analisis Cepat (Kosongkan dulu sesuai permintaan sebelumnya)
        dashboardItems.add(DashboardItem.AnalisisCepat(emptyList()))

        // 3. Tombol Aksi Cepat
        dashboardItems.add(DashboardItem.AksiCepat)

        // 4. Widget Inventaris (Hanya muncul jika data sudah ada)
        dataInv?.let {
            dashboardItems.add(DashboardItem.Inventaris(it))
        }

        // 5. List Tugas Pending (Kartu Induk yang berisi daftar tugas)
        if (listTugas.isNotEmpty()) {
            dashboardItems.add(DashboardItem.TugasPending(listTugas))
        }

        // Kirim data ke adapter
        dashboardAdapter.update(dashboardItems)
    }

    private fun setupFormListeners() {
        val formTugas = binding.layoutFormTambahTugas
        val formNotif = binding.layoutFormKirimNotifikasi

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

        formNotif.btnKirimNotifFirebase.setOnClickListener {
            val judul = formNotif.etJudulNotif.text.toString().trim()
            val pesan = formNotif.etPesanNotif.text.toString().trim()
            val target = formNotif.spinnerTargetNotif.selectedItem?.toString() ?: ""
            val villa = formNotif.spinnerVillaNotif.selectedItem?.toString() ?: ""
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
                "tipe" to tipeNotif
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
            switchUrgent.isChecked = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}