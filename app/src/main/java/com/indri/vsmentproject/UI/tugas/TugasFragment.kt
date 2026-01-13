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
            currentEditTaskId = null
            binding.layoutPilihVilla.visibility = View.VISIBLE
            viewModel.getVillaList()
        }
        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.layoutFormInput.root.visibility = View.GONE
            resetForm()
        }
        observeData()
    }

    private fun setupMainRecyclerView() {
        tugasVillaAdapter = TugasVillaAdapter(
            onItemClick = { tugas -> tampilkanDetailTugas(tugas) },
            onItemLongClick = { tugas -> konfirmasiHapus(tugas) }
        )
        binding.rvTugasVilla.apply {
            adapter = tugasVillaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getTugasGroupedByVilla()
    }

    private fun tampilkanDetailTugas(tugas: TugasModel) {
        val villaGroup = viewModel.tugasGrouped.value?.find { group -> group.listTugas.any { it.id == tugas.id } }
        val villaName = villaGroup?.namaVilla ?: ""

        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Tugas")
            .setMessage("Pilih tindakan untuk tugas:\n${tugas.tugas}")
            .setPositiveButton("Ubah/Edit") { _, _ ->
                // Jika pilih Edit, buka form input seperti biasa
                bukaFormEdit(tugas, villaName)
            }
            .setNeutralButton("Hapus") { _, _ ->
                // Jika pilih Hapus, langsung panggil konfirmasi hapus
                konfirmasiHapus(tugas)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun bukaFormEdit(tugas: TugasModel, villaName: String) {
        currentEditTaskId = tugas.id
        currentEditVillaName = villaName

        binding.layoutFormInput.root.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "Ubah: $villaName"
        binding.layoutFormInput.etNamaTugas.setText(tugas.tugas)
        binding.layoutFormInput.etDeskripsiTugas.setText(tugas.keterangan)
        tanggalTerpilih = tugas.deadline
        binding.layoutFormInput.tvTanggalTerpilih.text = tugas.deadline

        // Set Spinner Staff
        val staffAdapter = binding.layoutFormInput.spinnerStaff.adapter
        for (i in 0 until (staffAdapter?.count ?: 0)) {
            if (staffAdapter?.getItem(i).toString() == tugas.staff_nama) {
                binding.layoutFormInput.spinnerStaff.setSelection(i)
                break
            }
        }

        // Set RadioButton Kategori
        val radioGroup = binding.layoutFormInput.rgKategori
        for (i in 0 until radioGroup.childCount) {
            val rb = radioGroup.getChildAt(i) as? RadioButton
            if (rb?.text.toString().equals(tugas.kategori, true)) {
                rb?.isChecked = true
                break
            }
        }

        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan(villaName, null) }
    }
    private fun konfirmasiHapus(tugas: TugasModel) {
        val villaName = viewModel.tugasGrouped.value?.find { g -> g.listTugas.any { it.id == tugas.id } }?.namaVilla ?: ""
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Tugas")
            .setMessage("Hapus '${tugas.tugas}'?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.hapusTugas(villaName, tugas.id) { sukses ->
                    if (sukses) Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun prosesSimpan(namaVilla: String, namaRuangan: String?) {
        val namaTugasInput = binding.layoutFormInput.etNamaTugas.text.toString()
        val staff = binding.layoutFormInput.spinnerStaff.selectedItem?.toString() ?: ""
        val rbId = binding.layoutFormInput.rgKategori.checkedRadioButtonId
        val kategori = binding.root.findViewById<RadioButton>(rbId)?.text?.toString() ?: "Lainnya"

        if (namaTugasInput.isNotEmpty() && tanggalTerpilih.isNotEmpty()) {
            val finalNama = if (currentEditTaskId == null) "[$namaRuangan] $namaTugasInput" else namaTugasInput
            val data = mapOf(
                "tugas" to finalNama, "keterangan" to binding.layoutFormInput.etDeskripsiTugas.text.toString(),
                "kategori" to kategori, "staff_nama" to staff, "deadline" to tanggalTerpilih, "status" to "pending"
            )
            val cb = { sukses: Boolean ->
                if (sukses) {
                    binding.layoutFormInput.root.visibility = View.GONE
                    resetForm()
                    Toast.makeText(requireContext(), "Berhasil!", Toast.LENGTH_SHORT).show()
                }
            }
            if (currentEditTaskId == null) viewModel.simpanTugasLengkap(namaVilla, data, cb)
            else viewModel.updateTugas(namaVilla, currentEditTaskId!!, data, cb)
        }
    }

    private fun observeData() {
        viewModel.tugasGrouped.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                filterAndDisplay(data, "all")
                updateStatistics(data)
                binding.toggleGroupFilter.addOnButtonCheckedListener { _, id, isChecked ->
                    if (isChecked) {
                        val filter = when (id) {
                            R.id.btnPending -> "pending"
                            R.id.btnDone -> "selesai"
                            else -> "all"
                        }
                        filterAndDisplay(data, filter)
                        updateFilterButtonUI(id)
                    }
                }
            }
        }
        viewModel.villaList.observe(viewLifecycleOwner) { list -> if (!list.isNullOrEmpty()) villaAdapter.updateData(list) }
    }

    private fun filterAndDisplay(data: List<VillaTugasGroup>, status: String) {
        if (status == "all") tugasVillaAdapter.updateList(data)
        else {
            val filtered = data.map { g -> g.copy(listTugas = g.listTugas.filter { it.status.lowercase() == status.lowercase() }) }.filter { it.listTugas.isNotEmpty() }
            tugasVillaAdapter.updateList(filtered)
        }
    }

    private fun updateFilterButtonUI(selectedId: Int) {
        listOf(R.id.btnAll, R.id.btnPending, R.id.btnDone).forEach { id ->
            val btn = binding.root.findViewById<MaterialButton>(id)
            if (id == selectedId) { btn.setBackgroundColor(Color.WHITE); btn.setTextColor(Color.parseColor("#C2185B")) }
            else { btn.setBackgroundColor(Color.TRANSPARENT); btn.setTextColor(Color.WHITE) }
        }
    }

    private fun updateStatistics(allData: List<VillaTugasGroup>) {
        val allTasks = allData.flatMap { it.listTugas }
        val selesai = allTasks.count { it.status.lowercase() == "selesai" }
        val pending = allTasks.count { it.status.lowercase() == "pending" }
        val percent = if (allTasks.isNotEmpty()) (selesai * 100) / allTasks.size else 0
        binding.tvPercentValue.text = "$percent%"
        binding.tvPendingCount.text = pending.toString()
    }

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villaName ->
            val v = viewModel.villaList.value?.find { it.nama == villaName }
            if (v != null && v.area.isNotEmpty()) {
                val areas = v.area.toTypedArray()
                AlertDialog.Builder(requireContext()).setTitle("Pilih Ruangan").setItems(areas) { _, i -> bukaFormInput(v.nama, areas[i]) }.show()
            } else bukaFormInput(villaName, "Umum")
        }
        binding.rvPilihVilla.apply { layoutManager = LinearLayoutManager(requireContext()); adapter = villaAdapter }
    }

    private fun bukaFormInput(namaVilla: String, namaRuangan: String) {
        currentEditTaskId = null
        binding.layoutPilihVilla.visibility = View.GONE
        binding.layoutFormInput.root.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "$namaVilla - $namaRuangan"
        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan(namaVilla, namaRuangan) }
    }

    private fun setupStaffSpinner() {
        viewModel.getStaffList()
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            val names = list.map { it.nama }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutFormInput.spinnerStaff.adapter = adapter
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            tanggalTerpilih = String.format("%02d-%02d-%d", d, m + 1, y)
            binding.layoutFormInput.tvTanggalTerpilih.text = tanggalTerpilih
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun resetForm() {
        binding.layoutFormInput.etNamaTugas.text?.clear()
        binding.layoutFormInput.etDeskripsiTugas.text?.clear()
        tanggalTerpilih = ""
        binding.layoutFormInput.tvTanggalTerpilih.text = "Pilih Tanggal"
        currentEditTaskId = null
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}