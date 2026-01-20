package com.indri.vsmentproject.ui.manager.task

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.FragmentTugasBinding
import java.util.Calendar

class TugasFragment : Fragment() {
    private var _binding: FragmentTugasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TugasViewModel by viewModels()

    private lateinit var tugasVillaAdapter: TugasVillaAdapter
    private lateinit var villaAdapter: PilihVillaAdapter

    private var tanggalTerpilih = ""
    private var currentEditTaskId: String? = null
    private var currentVillaId: String? = null
    private var currentVillaName: String? = null
    private var currentRoom: String? = "Umum"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMainRecyclerView()
        setupPilihVillaAdapter()
        setupStaffSpinner()

        // FAB: Munculkan container pilih villa (SINKRON DENGAN XML TERBARU)
        binding.fabTambahTugas.setOnClickListener {
            binding.layoutPilihVillaContainer.visibility = View.VISIBLE
            viewModel.getVillaList()
        }

        binding.layoutFormInput.btnPilihTanggal.setOnClickListener { showDatePicker() }

        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.containerFormInput.visibility = View.GONE
            resetForm()
        }

        // NAVIGASI: Perbaikan ID Container agar tidak crash
        binding.btnLihatSemua.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProgresDetailFragment()) // Sesuaikan dengan ID di XML Activity
                .addToBackStack(null)
                .commit()
        }
        binding.toggleGroupFilter.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.filterTugas("All")
                    R.id.btnPending -> viewModel.filterTugas("pending")
                    R.id.btnDone -> viewModel.filterTugas("selesai")
                }
            }
        }

        observeData()
    }

    private fun setupMainRecyclerView() {
        tugasVillaAdapter = TugasVillaAdapter(
            onItemClick = { tugas -> bukaFormEdit(tugas) },
            onItemLongClick = { tugas -> konfirmasiHapus(tugas) }
        )
        binding.rvTugasVilla.apply {
            adapter = tugasVillaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getTugasGroupedByVilla()
    }

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villa ->
            currentVillaId = villa.id
            currentVillaName = villa.nama

            if (!villa.areas.isNullOrEmpty()) {
                val listRuangan = villa.areas.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Lokasi di ${villa.nama}")
                    .setItems(listRuangan) { _, i ->
                        bukaFormInput(villa.id, listRuangan[i])
                    }
                    .show()
            } else {
                bukaFormInput(villa.id, "Umum")
            }
        }
        binding.rvPilihVilla.adapter = villaAdapter
        binding.rvPilihVilla.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun bukaFormInput(villaId: String, namaRuangan: String) {
        currentEditTaskId = null
        currentVillaId = villaId
        currentRoom = namaRuangan

        // Sembunyikan layer pilih villa, munculkan layer form
        binding.layoutPilihVillaContainer.visibility = View.GONE
        binding.containerFormInput.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "$currentVillaName - $namaRuangan"
        resetForm()

        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan() }
    }

    private fun bukaFormEdit(tugas: TugasModel) {
        currentEditTaskId = tugas.id
        currentVillaId = tugas.villa_id
        currentVillaName = tugas.villa_nama
        currentRoom = tugas.ruangan

        binding.containerFormInput.visibility = View.VISIBLE
        binding.layoutFormInput.tvHeaderVilla.text = "Ubah: ${tugas.villa_nama} (${tugas.ruangan})"
        binding.layoutFormInput.etNamaTugas.setText(tugas.tugas)
        binding.layoutFormInput.etDeskripsiTugas.setText(tugas.deskripsi)

        tanggalTerpilih = tugas.deadline
        binding.layoutFormInput.tvTanggalTerpilih.text = tugas.deadline

        val listStaff = viewModel.staffList.value ?: emptyList()
        val idx = listStaff.indexOfFirst { it.uid == tugas.worker_id }
        if (idx != -1) binding.layoutFormInput.spinnerStaff.setSelection(idx)

        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan() }
    }

    private fun prosesSimpan() {
        val namaTugas = binding.layoutFormInput.etNamaTugas.text.toString()
        val staffPos = binding.layoutFormInput.spinnerStaff.selectedItemPosition
        val staffObj = viewModel.staffList.value?.getOrNull(staffPos)

        val rbId = binding.layoutFormInput.rgKategori.checkedRadioButtonId
        // CARA AMAN: Ambil RadioButton dari root form agar tidak ClassCastException
        val prioritas = binding.layoutFormInput.root.findViewById<RadioButton>(rbId)?.text?.toString() ?: "Medium"

        if (namaTugas.isNotEmpty() && staffObj != null && tanggalTerpilih.isNotEmpty() && currentVillaId != null) {
            val data = mapOf(
                "villa_id" to currentVillaId!!,
                "villa_nama" to currentVillaName!!,
                "ruangan" to currentRoom!!,
                "worker_id" to staffObj.uid,
                "worker_name" to staffObj.nama,
                "tugas" to namaTugas,
                "deskripsi" to binding.layoutFormInput.etDeskripsiTugas.text.toString(),
                "prioritas" to prioritas,
                "deadline" to tanggalTerpilih,
                "status" to "pending",
                "created_at" to System.currentTimeMillis()
            )

            val cb = { sukses: Boolean ->
                if (sukses) {
                    binding.containerFormInput.visibility = View.GONE
                    resetForm()
                    Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                }
            }

            if (currentEditTaskId == null) viewModel.simpanTugasLengkap(currentVillaId!!, data, cb)
            else viewModel.updateTugas(currentVillaId!!, currentEditTaskId!!, data, cb)
        } else {
            Toast.makeText(context, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun konfirmasiHapus(tugas: TugasModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Tugas?")
            .setMessage("Tugas '${tugas.tugas}' akan dihapus permanen. Lanjutkan?")
            .setCancelable(false)
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Ya, Hapus") { _, _ ->
                viewModel.hapusTugas(tugas.villa_id, tugas.id) { sukses ->
                    if (sukses) Toast.makeText(requireContext(), "Dihapus!", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }

    private fun observeData() {
        // 1. Observer untuk LIST BAWAH (Bisa berubah saat difilter)
        viewModel.tugasGrouped.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                tugasVillaAdapter.updateList(data)
            }
        }

        // 2. Observer untuk RINGKASAN ATAS (Gunakan data mentah agar angka TIDAK berubah)
        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val allTasks = data.flatMap { it.listTugas }
                val selesai = allTasks.count { it.status.equals("selesai", true) }
                val pending = allTasks.count { it.status.equals("pending", true) }
                val percent = if (allTasks.isNotEmpty()) (selesai * 100) / allTasks.size else 0

                binding.tvPercentValue.text = "$percent%"
                binding.tvPendingCount.text = pending.toString()
            }
        }

        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            if (list != null) villaAdapter.updateData(list)
        }
    }

    private fun setupStaffSpinner() {
        viewModel.getStaffList()
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list.map { it.nama })
                binding.layoutFormInput.spinnerStaff.adapter = adapter
            }
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