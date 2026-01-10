package com.indri.vsmentproject.UI.tugas

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.indri.vsmentproject.Data.Model.TugasModel
import com.indri.vsmentproject.Data.Model.VillaModel
import com.indri.vsmentproject.Data.Model.tugas.VillaTugasGroup
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentTugasBinding
import java.util.Calendar
import java.util.Locale

class TugasFragment : Fragment() {
    private var _binding: FragmentTugasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TugasViewModel by viewModels()

    private lateinit var tugasVillaAdapter: TugasVillaAdapter
    private lateinit var villaAdapter: PilihVillaAdapter
    private var tanggalTerpilih = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Adapters
        setupMainRecyclerView()
        setupPilihVillaAdapter()

        // 2. Setup Form Input
        setupStaffSpinner()
        binding.layoutFormInput.btnPilihTanggal.setOnClickListener { showDatePicker() }

        // 3. Logic Tombol Tambah & Batal
        binding.fabTambahTugas.setOnClickListener {
            binding.layoutPilihVilla.visibility = View.VISIBLE
            viewModel.getVillaList()
        }

        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.layoutFormInput.root.visibility = View.GONE
        }

        // 4. Observasi Data & Setup Filter
        observeData()
    }

    private fun setupMainRecyclerView() {
        // Kita kirimkan callback klik dari adapter ke fragment
        tugasVillaAdapter = TugasVillaAdapter { tugas ->
            tampilkanDetailTugas(tugas)
        }
        binding.rvTugasVilla.apply {
            adapter = tugasVillaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getTugasGroupedByVilla()
    }

    private fun observeData() {
        viewModel.tugasGrouped.observe(viewLifecycleOwner) { allData ->
            if (allData != null) {
                // Default tampilkan semua saat data pertama kali dimuat
                filterAndDisplay(allData, "all")
                updateStatistics(allData)

                // Logika Filter Tab (Putih-putih/Indikator pindah)
                binding.toggleGroupFilter.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        val filterType = when (checkedId) {
                            R.id.btnPending -> "pending"
                            R.id.btnDone -> "selesai"
                            else -> "all"
                        }
                        filterAndDisplay(allData, filterType)

                        // Menangani warna UI tombol secara manual agar "putih" nya pindah
                        updateFilterButtonUI(checkedId)
                    }
                }
            }
        }

        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) villaAdapter.updateData(list)
        }
    }

    private fun filterAndDisplay(data: List<VillaTugasGroup>, status: String) {
        if (status == "all") {
            tugasVillaAdapter.updateList(data)
        } else {
            val filteredData = data.map { villaGroup ->
                villaGroup.copy(listTugas = villaGroup.listTugas.filter {
                    it.status.lowercase(Locale.getDefault()) == status.lowercase(Locale.getDefault())
                })
            }.filter { it.listTugas.isNotEmpty() }
            tugasVillaAdapter.updateList(filteredData)
        }
    }

    // Fungsi untuk memindahkan indikator visual (Background Putih) pada Tab Filter
    private fun updateFilterButtonUI(selectedId: Int) {
        val buttons = listOf(R.id.btnAll, R.id.btnPending, R.id.btnDone)
        buttons.forEach { id ->
            val btn = binding.root.findViewById<MaterialButton>(id)
            if (id == selectedId) {
                btn.setBackgroundColor(Color.WHITE)
                btn.setTextColor(Color.parseColor("#C2185B")) // Warna Pink/Accent
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT)
                btn.setTextColor(Color.WHITE)
            }
        }
    }

    // FUNGSI DETAIL TUGAS (Muncul saat item di klik)
    private fun tampilkanDetailTugas(tugas: TugasModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Detail Tugas")
            .setMessage("""
                📌 Nama Tugas : ${tugas.tugas}
                📂 Kategori     : ${tugas.kategori}
                👤 Staff PIC    : ${tugas.staff_nama}
                📅 Deadline     : ${tugas.deadline}
                ⚡ Status       : ${tugas.status.uppercase()}
                
                📝 Keterangan:
                ${tugas.keterangan.ifEmpty { "-" }}
            """.trimIndent())
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Ubah Status") { _, _ ->
                // Kamu bisa tambah logic ganti status di sini nanti
                Toast.makeText(requireContext(), "Fitur ubah status segera hadir", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun updateStatistics(allData: List<VillaTugasGroup>) {
        // 1. Ambil semua tugas dari semua villa (flat list)
        val allTasks = allData.flatMap { it.listTugas }
        val totalTugas = allTasks.size

        // 2. Hitung jumlah yang 'selesai' dan 'pending'
        // Pastikan teks "selesai" & "pending" sesuai dengan yang ada di Firebase
        val tugasSelesai = allTasks.count { it.status.lowercase() == "selesai" }
        val tugasPending = allTasks.count { it.status.lowercase() == "pending" }

        // 3. Hitung persentase progres
        val persentase = if (totalTugas > 0) (tugasSelesai * 100) / totalTugas else 0

        // 4. Update ke UI (Pastikan ID-nya sesuai dengan fragment_tugas.xml)
        binding.tvPercentValue.text = "$persentase%"

        // BARIS INI YANG SERING TERLUPA:
        binding.tvPendingCount.text = tugasPending.toString()
    }

    // --- Sisanya adalah fungsi pendukung form input ---

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villaSelected ->
            val villaData = viewModel.villaList.value?.find { it.nama == villaSelected }
            if (villaData != null && villaData.area.isNotEmpty()) {
                tampilkanPilihanRuangan(villaData)
            } else {
                bukaFormInput(villaSelected, "Umum")
            }
        }
        binding.rvPilihVilla.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = villaAdapter
        }
    }

    private fun setupStaffSpinner() {
        viewModel.getStaffList()
        viewModel.staffList.observe(viewLifecycleOwner) { listStaff ->
            val names = listStaff.map { it.nama }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutFormInput.spinnerStaff.adapter = adapter
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            tanggalTerpilih = String.format("%02d-%02d-%d", day, month + 1, year)
            binding.layoutFormInput.tvTanggalTerpilih.text = tanggalTerpilih
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun tampilkanPilihanRuangan(villa: VillaModel) {
        val daftarArea = villa.area.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Ruangan")
            .setItems(daftarArea) { _, which -> bukaFormInput(villa.nama, daftarArea[which]) }
            .show()
    }

    private fun bukaFormInput(namaVilla: String, namaRuangan: String) {
        binding.layoutPilihVilla.visibility = View.GONE
        binding.layoutFormInput.root.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "$namaVilla - $namaRuangan"

        binding.layoutFormInput.btnSimpan.setOnClickListener {
            val namaTugas = binding.layoutFormInput.etNamaTugas.text.toString()
            val staff = binding.layoutFormInput.spinnerStaff.selectedItem?.toString() ?: ""
            val selectedRbId = binding.layoutFormInput.rgKategori.checkedRadioButtonId
            val kategori = binding.root.findViewById<RadioButton>(selectedRbId)?.text?.toString() ?: "Lainnya"

            if (namaTugas.isNotEmpty() && tanggalTerpilih.isNotEmpty()) {
                val data = mapOf(
                    "tugas" to "[$namaRuangan] $namaTugas",
                    "keterangan" to binding.layoutFormInput.etDeskripsiTugas.text.toString(),
                    "kategori" to kategori,
                    "staff_nama" to staff,
                    "deadline" to tanggalTerpilih,
                    "status" to "pending"
                )
                viewModel.simpanTugasLengkap(namaVilla, data) { sukses ->
                    if (sukses) {
                        binding.layoutFormInput.root.visibility = View.GONE
                        resetForm()
                        Toast.makeText(requireContext(), "Berhasil!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun resetForm() {
        binding.layoutFormInput.etNamaTugas.text?.clear()
        binding.layoutFormInput.etDeskripsiTugas.text?.clear()
        tanggalTerpilih = ""
        binding.layoutFormInput.tvTanggalTerpilih.text = "Pilih Tanggal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}