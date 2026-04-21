package com.indri.vsmentproject.ui.manager.task


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.databinding.FragmentTugasBinding
import com.indri.vsmentproject.ui.manager.task.progressVilla.ProgresDetailFragment
import com.indri.vsmentproject.ui.manager.task.progressVilla.ProgresVillaAdapter
import com.indri.vsmentproject.ui.manager.template.FragmentTemplateForm
import java.util.*

class TugasFragment : Fragment() {

    private var _binding: FragmentTugasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TugasViewModel by viewModels()

    // Menggunakan Container Adapter untuk struktur Waktu > Card > List Villa/Tugas
    private lateinit var containerAdapter: WaktuContainerAdapter
    private lateinit var villaAdapter: PilihVillaAdapter

    private var tanggalTerpilih = ""
    private var currentEditTaskId: String? = null
    private var currentVillaId: String? = null
    private var currentVillaName: String? = null
    private var currentRoom: String = "Umum"

    private lateinit var progresVillaAdapter: ProgresVillaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPilihVillaAdapter()
        setupStaffSpinner()
        setupAction()
        observeData()

        // Memicu pengambilan data dengan grouping terbaru
        viewModel.getTugasGroupedByVilla()
    }

    private fun setupRecyclerView() {
        // Setup adapter utama yang membungkus kategori waktu dalam Card
        progresVillaAdapter = ProgresVillaAdapter()
        // 2. Setup List Tugas (Yang Card Waktu > Villa > Tugas)
        containerAdapter = WaktuContainerAdapter()
        binding.rvTugasVilla.apply { // PAKAI ID rvTugasVilla
            adapter = containerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villa ->
            currentVillaId = villa.id
            currentVillaName = villa.nama

            if (!villa.area.isNullOrEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Lokasi di ${villa.nama}")
                    .setItems(villa.area.toTypedArray()) { _, i ->
                        bukaFormInput(villa.id, villa.area[i])
                    }
                    .show()
            } else {
                bukaFormInput(villa.id, "Umum")
            }
        }

        binding.rvPilihVilla.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = villaAdapter
        }
    }

    private fun setupStaffSpinner() {
        viewModel.getStaffList()
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            binding.layoutFormInput.spinnerStaff.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.nama }
            )
        }
    }

    private fun setupAction() {
        binding.fabTambahTugas.setOnClickListener {
            binding.layoutPilihVillaContainer.visibility = View.VISIBLE
            viewModel.getVillaList()
        }

        binding.fabTambahTugas.setOnLongClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTemplateForm())
                .addToBackStack(null)
                .commit()
            true
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

    private fun observeData() {
        // Mengamati data yang sudah di-group menjadi WaktuContainer (Card Per Waktu)
        viewModel.waktuListLive.observe(viewLifecycleOwner) { listWaktu ->
            if (!listWaktu.isNullOrEmpty()) {
                containerAdapter.submitList(listWaktu)
            }
        }

        // Statistik presentase
        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { data ->
            val all = data.flatMap { it.listTugas }
            val selesai = all.count { it.status.equals("selesai", true) }
            val pending = all.count { it.status.equals("pending", true) }
            val percent = if (all.isNotEmpty()) (selesai * 100) / all.size else 0

            binding.tvPercentValue.text = "$percent%"
            binding.tvPendingCount.text = pending.toString()
        }

        viewModel.villaList.observe(viewLifecycleOwner) {
            villaAdapter.updateData(it)
        }
    }

    private fun bukaFormInput(villaId: String, ruangan: String) {
        currentEditTaskId = null
        currentVillaId = villaId
        currentRoom = ruangan

        binding.layoutPilihVillaContainer.visibility = View.GONE
        binding.containerFormInput.visibility = View.VISIBLE

        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan() }
    }

    private fun bukaFormEdit(tugas: TugasModel) {
        currentEditTaskId = tugas.id
        currentVillaId = tugas.villa_id
        currentVillaName = tugas.villa_nama
        currentRoom = tugas.ruangan

        binding.containerFormInput.visibility = View.VISIBLE

        binding.layoutFormInput.etNamaTugas.setText(tugas.tugas)
        binding.layoutFormInput.etDeskripsiTugas.setText(tugas.deskripsi)
        tanggalTerpilih = tugas.deadline

        binding.layoutFormInput.btnSimpan.setOnClickListener { prosesSimpan() }
    }

    private fun prosesSimpan() {
        val nama = binding.layoutFormInput.etNamaTugas.text.toString()

        if (nama.isEmpty() || currentVillaId == null) {
            Toast.makeText(context, "Isi data!", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "villa_id" to currentVillaId!!,
            "villa_nama" to (currentVillaName ?: ""),
            "ruangan" to currentRoom,
            "tugas" to nama,
            "deadline" to tanggalTerpilih,
            "status" to "pending"
        )

        val cb = { ok: Boolean ->
            if (ok) {
                binding.containerFormInput.visibility = View.GONE
                resetForm()
            }
        }

        if (currentEditTaskId == null)
            viewModel.simpanTugasLengkap(currentVillaId!!, data, cb)
        else
            viewModel.updateTugas(currentVillaId!!, currentEditTaskId!!, data, cb)
    }

    private fun konfirmasiHapus(tugas: TugasModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.hapusTugas(tugas.villa_id, tugas.id) {}
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            tanggalTerpilih = "%02d-%02d-%d".format(d, m + 1, y)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun resetForm() {
        binding.layoutFormInput.etNamaTugas.text?.clear()
        binding.layoutFormInput.etDeskripsiTugas.text?.clear()
        tanggalTerpilih = ""
        currentEditTaskId = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}