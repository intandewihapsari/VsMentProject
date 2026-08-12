package com.indri.vsmentproject.ui.manager.task

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentTugasBinding
import com.indri.vsmentproject.ui.manager.task.progressVilla.ProgresDetailFragment
import com.indri.vsmentproject.ui.manager.template.FragmentTemplateForm
import java.util.*

class TugasFragment : Fragment() {

    private var _binding: FragmentTugasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TugasViewModel by viewModels()
    private lateinit var containerAdapter: WaktuContainerAdapter
    private lateinit var villaAdapter: PilihVillaAdapter

    private var tanggalTerpilih = ""
    private var currentEditTaskId: String? = null
    private var currentVillaId: String? = null
    private var currentVillaName: String? = null
    private var currentRoom: String = "Umum"
    private var managerUid = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupRecyclerView()
        setupPilihVillaAdapter()
        setupStaffSpinner()
        setupAction()
        observeData()

        if (managerUid.isNotEmpty()) {
            viewModel.getTugasGroupedByVilla(managerUid)
        }

        // 🔥 JIKA TERBACA ARGUMEN, LANGSUNG BUKA PILIH VILLA
        if (arguments?.getBoolean("OPEN_ADD_TASK") == true) {
            bukaPilihVillaOverlay()
        }
    }

    private fun bukaPilihVillaOverlay() {
        if (_binding != null && isAdded) {
            binding.layoutPilihVillaContainer.visibility = View.VISIBLE
            if (managerUid.isNotEmpty()) {
                viewModel.getVillaList(managerUid)
            }
        }
    }

    private fun setupRecyclerView() {
        containerAdapter = WaktuContainerAdapter()
        binding.rvTugasVilla.apply {
            adapter = containerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupPilihVillaAdapter() {
        villaAdapter = PilihVillaAdapter { villa ->
            currentVillaId = villa.id
            currentVillaName = villa.nama

            if (!villa.area.isNullOrEmpty()) {
                val dialogView = layoutInflater.inflate(R.layout.dialog_area, null)
                val rvArea = dialogView.findViewById<RecyclerView>(R.id.rvArea)

                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Lokasi di ${villa.nama}")
                    .setView(dialogView)
                    .create()

                rvArea.layoutManager = LinearLayoutManager(requireContext())
                rvArea.adapter = AreaAdapter(villa.area) { selectedArea ->
                    dialog.dismiss()
                    bukaFormInput(villa.id, selectedArea)
                }
                dialog.show()
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
        if (managerUid.isNotEmpty()) {
            viewModel.getStaffList(managerUid)
        }
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
            bukaPilihVillaOverlay()
        }

        binding.fabTambahTugas.setOnLongClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTemplateForm())
                .addToBackStack(null).commit()
            true
        }

        binding.layoutFormInput.btnPilihTanggal.setOnClickListener { showDatePicker() }

        binding.layoutFormInput.btnBatal.setOnClickListener {
            binding.containerFormInput.visibility = View.GONE
            resetForm()
        }

        binding.btnLihatSemua.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProgresDetailFragment())
                .addToBackStack(null).commit()
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
        viewModel.waktuListLive.observe(viewLifecycleOwner) { listWaktu ->
            if (!listWaktu.isNullOrEmpty()) {
                containerAdapter.submitList(listWaktu)
            }
        }

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

    private fun prosesSimpan() {
        val nama = binding.layoutFormInput.etNamaTugas.text.toString().trim()
        val staffTerpilih = binding.layoutFormInput.spinnerStaff.selectedItem?.toString() ?: ""
        val staffObject = viewModel.staffList.value?.find { it.nama == staffTerpilih }

        if (nama.isEmpty() || currentVillaId == null) {
            Toast.makeText(context, "Harap lengkapi isi data!", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "villa_id" to currentVillaId!!,
            "villa_nama" to (currentVillaName ?: ""),
            "ruangan" to currentRoom,
            "tugas" to nama,
            "deadline" to tanggalTerpilih,
            "status" to "pending",
            "staff_id" to (staffObject?.uid ?: ""),
            "staff_nama" to staffTerpilih
        )

        val cb = { ok: Boolean ->
            if (ok) {
                binding.containerFormInput.visibility = View.GONE
                resetForm()
            }
        }

        if (currentEditTaskId == null)
            viewModel.simpanTugasLengkap(managerUid, currentVillaId!!, data, cb)
        else
            viewModel.updateTugas(managerUid, currentVillaId!!, currentEditTaskId!!, data, cb)
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, y, m, d ->
            tanggalTerpilih = "%02d-%02d-%d".format(d, m + 1, y)
            binding.layoutFormInput.tvTanggalTerpilih.apply {
                text = tanggalTerpilih
                setTextColor(resources.getColor(android.R.color.black))
            }
        }, year, month, day)

        datePickerDialog.datePicker.minDate = c.timeInMillis
        datePickerDialog.show()
    }

    private fun resetForm() {
        binding.layoutFormInput.etNamaTugas.text?.clear()
        binding.layoutFormInput.etDeskripsiTugas.text?.clear()
        binding.layoutFormInput.tvTanggalTerpilih.apply {
            text = "Pilih Tanggal"
            setTextColor(resources.getColor(android.R.color.darker_gray))
        }
        binding.layoutFormInput.rgKategori.clearCheck()

        tanggalTerpilih = ""
        currentEditTaskId = null
    }

    // =========================================================
    // HELPER FOR BACK NAVIGATION (DIPANGGIL OLEH MANAGERACTIVITY)
    // =========================================================

    fun isFormOverlayOpen(): Boolean {
        return _binding != null && (
                binding.layoutPilihVillaContainer.visibility == View.VISIBLE ||
                        binding.containerFormInput.visibility == View.VISIBLE
                )
    }

    fun closeFormOverlay() {
        if (_binding != null) {
            if (binding.containerFormInput.visibility == View.VISIBLE) {
                binding.containerFormInput.visibility = View.GONE
                resetForm()
            } else if (binding.layoutPilihVillaContainer.visibility == View.VISIBLE) {
                binding.layoutPilihVillaContainer.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}