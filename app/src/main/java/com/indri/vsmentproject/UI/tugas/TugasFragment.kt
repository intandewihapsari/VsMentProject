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
    private var currentEditTaskId: String? = null
    private var currentEditVillaName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMainRecyclerView()
        setupPilihVillaAdapter()
        setupStaffSpinner()

        binding.layoutFormInput.btnPilihTanggal.setOnClickListener { showDatePicker() }

        binding.fabTambahTugas.setOnClickListener {
            currentEditTaskId = null // Mode Tambah
            binding.layoutPilihVilla.visibility = View.VISIBLE
            viewModel.getVillaList()
        }

        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.layoutFormInput.root.visibility = View.GONE
            resetForm()
        }

        observeData()
    }

    private fun observeData() {
        viewModel.tugasGrouped.observe(viewLifecycleOwner) { allData ->
            if (allData != null) {
                filterAndDisplay(allData, "all")
                updateStatistics(allData)
                binding.toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        val filterType = when (checkedId) {
                            R.id.btnPending -> "pending"
                            R.id.btnDone -> "selesai"
                            else -> "all"
                        }
                        filterAndDisplay(allData, filterType)
                        updateFilterButtonUI(checkedId)
                    }
                }
            }
        }
        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) villaAdapter.updateData(list)
        }
    }

    private fun tampilkanDetailTugas(tugas: TugasModel) {
        // 1. Identifikasi Villa
        val villaGroup = viewModel.tugasGrouped.value?.find { group ->
            group.listTugas.any { it.id == tugas.id }
        }

        currentEditTaskId = tugas.id
        currentEditVillaName = villaGroup?.namaVilla ?: ""

        // 2. Tampilkan Form & Header
        binding.layoutFormInput.root.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "Ubah: $currentEditVillaName"

        // 3. Isi Kolom Teks
        binding.layoutFormInput.etNamaTugas.setText(tugas.tugas)
        binding.layoutFormInput.etDeskripsiTugas.setText(tugas.keterangan)

        // 4. Isi Tanggal
        tanggalTerpilih = tugas.deadline
        binding.layoutFormInput.tvTanggalTerpilih.text = tugas.deadline

        // 5. Pilih Spinner Staff secara otomatis
        val staffAdapter = binding.layoutFormInput.spinnerStaff.adapter
        if (staffAdapter != null) {
            for (i in 0 until staffAdapter.count) {
                if (staffAdapter.getItem(i).toString() == tugas.staff_nama) {
                    binding.layoutFormInput.spinnerStaff.setSelection(i)
                    break
                }
            }
        }

        // 6. PILIH RADIO BUTTON KATEGORI (Agar tidak ngisi dari nol)
        val radioGroup = binding.layoutFormInput.rgKategori
        for (i in 0 until radioGroup.childCount) {
            val view = radioGroup.getChildAt(i)
            if (view is RadioButton) {
                // Jika teks di tombol sama dengan kategori di database, centang!
                if (view.text.toString().equals(tugas.kategori, ignoreCase = true)) {
                    view.isChecked = true
                    break
                }
            }
        }

        // 7. Update Listener Tombol Simpan agar menjalankan proses update
        binding.layoutFormInput.btnSimpan.setOnClickListener {
            prosesSimpan(currentEditVillaName!!, null)
        }
    }
    private fun bukaFormInput(namaVilla: String, namaRuangan: String) {
        currentEditTaskId = null
        binding.layoutPilihVilla.visibility = View.GONE
        binding.layoutFormInput.root.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "$namaVilla - $namaRuangan"

        binding.layoutFormInput.btnSimpan.setOnClickListener {
            prosesSimpan(namaVilla, namaRuangan)
        }
    }

    private fun prosesSimpan(namaVilla: String, namaRuangan: String?) {
        val namaTugasInput = binding.layoutFormInput.etNamaTugas.text.toString()
        val staff = binding.layoutFormInput.spinnerStaff.selectedItem?.toString() ?: ""
        val selectedRbId = binding.layoutFormInput.rgKategori.checkedRadioButtonId
        val kategori = binding.root.findViewById<RadioButton>(selectedRbId)?.text?.toString() ?: "Lainnya"

        if (namaTugasInput.isNotEmpty() && tanggalTerpilih.isNotEmpty()) {
            // Jika tambah baru, pakai tag [Ruangan], jika edit pakai inputan langsung
            val finalNamaTugas = if (currentEditTaskId == null) "[$namaRuangan] $namaTugasInput" else namaTugasInput

            val data = mapOf(
                "tugas" to finalNamaTugas,
                "keterangan" to binding.layoutFormInput.etDeskripsiTugas.text.toString(),
                "kategori" to kategori,
                "staff_nama" to staff,
                "deadline" to tanggalTerpilih,
                "status" to "pending"
            )

            val callback = { sukses: Boolean ->
                if (sukses) {
                    binding.layoutFormInput.root.visibility = View.GONE
                    resetForm()
                    Toast.makeText(requireContext(), "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
            }

            if (currentEditTaskId == null) {
                viewModel.simpanTugasLengkap(namaVilla, data, callback)
            } else {
                viewModel.updateTugas(namaVilla, currentEditTaskId!!, data, callback)
            }
        } else {
            Toast.makeText(requireContext(), "Lengkapi data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMainRecyclerView() {
        tugasVillaAdapter = TugasVillaAdapter { tugas -> tampilkanDetailTugas(tugas) }
        binding.rvTugasVilla.apply {
            adapter = tugasVillaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getTugasGroupedByVilla()
    }

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

    private fun updateFilterButtonUI(selectedId: Int) {
        val buttons = listOf(R.id.btnAll, R.id.btnPending, R.id.btnDone)
        buttons.forEach { id ->
            val btn = binding.root.findViewById<MaterialButton>(id)
            if (id == selectedId) {
                btn.setBackgroundColor(Color.WHITE)
                btn.setTextColor(Color.parseColor("#C2185B"))
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT)
                btn.setTextColor(Color.WHITE)
            }
        }
    }

    private fun updateStatistics(allData: List<VillaTugasGroup>) {
        val allTasks = allData.flatMap { it.listTugas }
        val totalTugas = allTasks.size
        val tugasSelesai = allTasks.count { it.status.lowercase() == "selesai" }
        val tugasPending = allTasks.count { it.status.lowercase() == "pending" }
        val persentase = if (totalTugas > 0) (tugasSelesai * 100) / totalTugas else 0
        binding.tvPercentValue.text = "$persentase%"
        binding.tvPendingCount.text = tugasPending.toString()
    }

    private fun tampilkanPilihanRuangan(villa: VillaModel) {
        val daftarArea = villa.area.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Pilih Ruangan")
            .setItems(daftarArea) { _, which -> bukaFormInput(villa.nama, daftarArea[which]) }
            .show()
    }

    private fun resetForm() {
        binding.layoutFormInput.etNamaTugas.text?.clear()
        binding.layoutFormInput.etDeskripsiTugas.text?.clear()
        tanggalTerpilih = ""
        binding.layoutFormInput.tvTanggalTerpilih.text = "Pilih Tanggal"
        currentEditTaskId = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}