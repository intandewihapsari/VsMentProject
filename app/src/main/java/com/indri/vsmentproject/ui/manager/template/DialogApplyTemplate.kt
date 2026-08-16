package com.indri.vsmentproject.ui.manager.template

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.databinding.DialogApplyTemplateBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DialogApplyTemplate(
    private val template: TaskTemplateModel,
    private val listVilla: List<Pair<String, String>>, // Pair(villaId, villaNama)
    private val listStaffMaster: List<Pair<String, String>>, // Pair(staffId, staffNama)
    private val onConfirm: (villaId: String, villaNama: String, ruangan: String, selectedStaff: List<Pair<String, String>>, deadline: String) -> Unit
) : BottomSheetDialogFragment() {


    private var _binding: DialogApplyTemplateBinding? = null
    private val binding get() = _binding!!

    private val checkBoxStaffMap = mutableMapOf<CheckBox, Pair<String, String>>()
    private val calendarDeadline = Calendar.getInstance()
    private var isDeadlineSelected = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogApplyTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvJudulTemplate.text = "Terapkan: ${template.nama_template}"
        binding.tvJumlahSubTugas.text = "Template ini berisi ${template.list_tugas_item.size} sub-tugas"

        // 1. Setup Spinner Villa
        val namaVillaList = listVilla.map { it.second }
        val villaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, namaVillaList)
        binding.spinnerVilla.adapter = villaAdapter

        // 2. Setup Spinner Ruangan Target
        val listRuanganPreset = listOf(
            "Kamar Utama (Master Bedroom)",
            "Kamar Tidur 02",
            "Kamar Tidur 03",
            "Dapur & Ruang Makan",
            "Ruang Tamu / Family Room",
            "Area Kolam Renang & Taman",
            "Toilet / Kamar Mandi",
            "Area Lobby & Teras Depan",
            "Seluruh Area Villa"
        )
        val ruanganAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listRuanganPreset)
        binding.spinnerRuangan.adapter = ruanganAdapter

        // 3. Render Dynamic Checkbox Staff Target
        listStaffMaster.forEach { staffPair ->
            val checkBox = CheckBox(requireContext()).apply {
                text = staffPair.second // Nama Staff
                textSize = 14f
            }
            checkBoxStaffMap[checkBox] = staffPair
            binding.layoutStaffList.addView(checkBox)
        }

        // 4. Picker Tanggal & Jam untuk Deadline
        binding.tvSelectDeadline.setOnClickListener {
            showDateTimePicker()
        }

        // 5. Action Kirim Tugas
        binding.btnKirimTugas.setOnClickListener {
            val selectedVillaPair = listVilla.getOrNull(binding.spinnerVilla.selectedItemPosition)
            val selectedRuangan = binding.spinnerRuangan.selectedItem?.toString() ?: ""
            val selectedStaffList = checkBoxStaffMap.filter { it.key.isChecked }.values.toList()

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val formattedDeadline = if (isDeadlineSelected) sdf.format(calendarDeadline.time) else ""

            if (selectedVillaPair == null) {
                Toast.makeText(context, "Pilih Villa target terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedStaffList.isEmpty()) {
                Toast.makeText(context, "Pilih minimal 1 Staff penanggung jawab!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isDeadlineSelected) {
                Toast.makeText(context, "Silakan pilih Tanggal & Jam deadline!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Eksekusi Panggilan Konfirmasi
            onConfirm(
                selectedVillaPair.first,  // villaId
                selectedVillaPair.second, // villaNama
                selectedRuangan,          // ruangan dari Dropdown
                selectedStaffList,       // List Staff pilihan (multi-select)
                formattedDeadline        // Deadline terformat: "15 Aug 2026, 14:00 WITA"
            )
            dismiss()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()

        // Pop-up 1: Pilih Tanggal
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendarDeadline.set(Calendar.YEAR, year)
                calendarDeadline.set(Calendar.MONTH, month)
                calendarDeadline.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Pop-up 2: Lanjut Pilih Jam
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendarDeadline.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendarDeadline.set(Calendar.MINUTE, minute)

                        isDeadlineSelected = true
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WITA'", Locale("id", "ID"))
                        binding.tvSelectDeadline.text = "📅 ${sdf.format(calendarDeadline.time)}"
                        binding.tvSelectDeadline.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.black)
                        )
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    true // Format 24 jam
                )
                timePicker.show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Batasi agar tidak bisa memilih tanggal kemarin
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
