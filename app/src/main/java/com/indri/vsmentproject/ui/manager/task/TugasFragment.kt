package com.indri.vsmentproject.ui.manager.task

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.FragmentTugasBinding
import java.util.Calendar
import kotlin.collections.getOrNull
import kotlin.collections.indexOfFirst
import kotlin.collections.map

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
    private var currentRoom: String = "Umum"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupPilihVillaAdapter()
        setupStaffSpinner()
        setupAction()
        observeData()

        viewModel.getTugasGroupedByVilla()
    }

    // ================= SETUP =================

    private fun setupAction() {

        binding.fabTambahTugas.setOnClickListener {
            binding.layoutPilihVillaContainer.visibility = View.VISIBLE
            viewModel.getVillaList()
        }

        binding.layoutFormInput.btnPilihTanggal.setOnClickListener {
            showDatePicker()
        }

        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.containerFormInput.visibility = View.GONE
            resetForm()
        }

        binding.btnLihatSemua.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProgresDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.filterTugas("All")
                    R.id.btnPending -> viewModel.filterTugas("pending")
                    R.id.btnDone -> viewModel.filterTugas("selesai")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        tugasVillaAdapter = TugasVillaAdapter(
            onItemClick = { bukaFormEdit(it) },
            onItemLongClick = { konfirmasiHapus(it) }
        )

        binding.rvTugasVilla.apply {
            adapter = tugasVillaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villa ->
            currentVillaId = villa.id
            currentVillaName = villa.nama

            if (!villa.area.isNullOrEmpty()) {
                val list = villa.area.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Lokasi di ${villa.nama}")
                    .setItems(list) { _, i ->
                        bukaFormInput(villa.id, list[i])
                    }
                    .show()
            } else {
                bukaFormInput(villa.id, "Umum")
            }
        }

        binding.rvPilihVilla.apply {
            adapter = villaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupStaffSpinner() {
        viewModel.getStaffList()

        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    list.map { user -> user.nama }
                )
                binding.layoutFormInput.spinnerStaff.adapter = adapter
            }
        }
    }

    // ================= FORM =================

    private fun bukaFormInput(villaId: String, ruangan: String) {
        currentEditTaskId = null
        currentVillaId = villaId
        currentRoom = ruangan

        binding.layoutPilihVillaContainer.visibility = View.GONE
        binding.containerFormInput.visibility = View.VISIBLE

        binding.layoutFormInput.tvHeaderVilla.text =
            "${currentVillaName ?: "-"} - $ruangan"

        resetForm()

        binding.layoutFormInput.btnSimpan.setOnClickListener {
            prosesSimpan()
        }
    }

    private fun bukaFormEdit(tugas: TugasModel) {
        currentEditTaskId = tugas.id
        currentVillaId = tugas.villa_id
        currentVillaName = tugas.villa_nama
        currentRoom = tugas.ruangan

        binding.containerFormInput.visibility = View.VISIBLE

        binding.layoutFormInput.tvHeaderVilla.text =
            "Edit: ${tugas.villa_nama} (${tugas.ruangan})"

        binding.layoutFormInput.etNamaTugas.setText(tugas.tugas)
        binding.layoutFormInput.etDeskripsiTugas.setText(tugas.deskripsi)

        tanggalTerpilih = tugas.deadline
        binding.layoutFormInput.tvTanggalTerpilih.text = tugas.deadline

        val listStaff = viewModel.staffList.value ?: emptyList()
        val index = listStaff.indexOfFirst { it.uid == tugas.staff_id }
        if (index != -1) {
            binding.layoutFormInput.spinnerStaff.setSelection(index)
        }

        binding.layoutFormInput.btnSimpan.setOnClickListener {
            prosesSimpan()
        }
    }

    private fun prosesSimpan() {
        val namaTugas = binding.layoutFormInput.etNamaTugas.text.toString().trim()
        val staffPos = binding.layoutFormInput.spinnerStaff.selectedItemPosition
        val staffObj = viewModel.staffList.value?.getOrNull(staffPos)

        val rbId = binding.layoutFormInput.rgKategori.checkedRadioButtonId
        val prioritas = binding.layoutFormInput.root
            .findViewById<RadioButton>(rbId)
            ?.text?.toString() ?: "Medium"

        val managerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (namaTugas.isNotEmpty() &&
            staffObj != null &&
            tanggalTerpilih.isNotEmpty() &&
            currentVillaId != null &&
            currentVillaName != null
        ) {

            val data = mapOf(
                "villa_id" to currentVillaId!!,
                "villa_nama" to currentVillaName!!,
                "ruangan" to currentRoom,

                "manager_id" to managerId,
                "staff_id" to staffObj.uid,
                "staff_name" to staffObj.nama,

                "tugas" to namaTugas,
                "deskripsi" to binding.layoutFormInput.etDeskripsiTugas.text.toString(),
                "prioritas" to prioritas,
                "deadline" to tanggalTerpilih,

                "status" to "pending",
                "created_at" to System.currentTimeMillis(),
                "completed_at" to 0
            )

            val callback = { sukses: Boolean ->
                if (sukses) {
                    binding.containerFormInput.visibility = View.GONE
                    resetForm()
                    Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                }
            }

            if (currentEditTaskId == null) {
                viewModel.simpanTugasLengkap(currentVillaId!!, data, callback)
            } else {
                viewModel.updateTugas(currentVillaId!!, currentEditTaskId!!, data, callback)
            }

        } else {
            Toast.makeText(context, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun konfirmasiHapus(tugas: TugasModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Tugas?")
            .setMessage("Tugas '${tugas.tugas}' akan dihapus permanen.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.hapusTugas(tugas.villa_id, tugas.id) {
                    Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }

    // ================= OBSERVER =================

    private fun observeData() {
        viewModel.tugasGrouped.observe(viewLifecycleOwner) {
            tugasVillaAdapter.updateList(it)
        }

        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { data ->
            val all = data.flatMap { it.listTugas }

            val selesai = all.count { it.status == "selesai" }
            val pending = all.count { it.status == "pending" }
            val percent = if (all.isNotEmpty()) (selesai * 100) / all.size else 0

            binding.tvPercentValue.text = "$percent%"
            binding.tvPendingCount.text = pending.toString()
        }

        viewModel.villaList.observe(viewLifecycleOwner) {
            villaAdapter.updateData(it)
        }
    }

    // ================= UTIL =================

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
        binding.layoutFormInput.rgKategori.clearCheck()

        tanggalTerpilih = ""
        binding.layoutFormInput.tvTanggalTerpilih.text = "Pilih Tanggal"

        currentEditTaskId = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}