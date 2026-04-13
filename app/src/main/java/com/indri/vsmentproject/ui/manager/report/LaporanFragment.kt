package com.indri.vsmentproject.ui.manager.report

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentLaporanBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LaporanViewModel by viewModels()
    private lateinit var laporanAdapter: LaporanAdapter

    private var currentFilterStatus = "semua"
    private var currentFilterVilla = "Semua Villa"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterStatus()
        observeData()
    }

    private fun setupRecyclerView() {
        laporanAdapter = LaporanAdapter { laporan ->
            tampilkanDetailLaporan(laporan)
        }

        binding.rvLaporan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = laporanAdapter
        }
    }

    private fun observeData() {
        viewModel.laporanResource.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Optional: tampilkan loading
                }

                is Resource.Success -> {
                    val list = resource.data ?: emptyList()
                    setupSpinnerVilla(list)
                    applyFilter(list)
                }

                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFilterStatus() {
        binding.toggleGroupStatus.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentFilterStatus = when (checkedId) {
                    R.id.btnBelum -> "pending"
                    R.id.btnSelesai -> "selesai"
                    else -> "semua"
                }

                applyFilter(viewModel.laporanResource.value?.data ?: emptyList())
            }
        }
    }

    private fun setupSpinnerVilla(list: List<LaporanModel>) {
        val villas = mutableListOf("Semua Villa")
        villas.addAll(list.map { it.villa_nama }.distinct())

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            villas
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterVilla.adapter = adapter

        binding.spinnerFilterVilla.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    currentFilterVilla = villas[pos]
                    applyFilter(viewModel.laporanResource.value?.data ?: emptyList())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun applyFilter(list: List<LaporanModel>) {
        var filteredList = list

        // 1. Filter Status
        if (currentFilterStatus != "semua") {
            filteredList = filteredList.filter {
                val status = it.status.lowercase()
                status == currentFilterStatus.lowercase()
            }
        }

        // 2. Filter Villa
        if (currentFilterVilla != "Semua Villa") {
            filteredList = filteredList.filter {
                it.villa_nama == currentFilterVilla
            }
        }

        // 3. SORTING TERBARU 🔥 (PALING PENTING)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        filteredList = filteredList.sortedByDescending {
            try {
                sdf.parse(it.waktu_lapor)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        // 4. Update UI
        if (filteredList.isEmpty()) {
            binding.rvLaporan.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvLaporan.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE

            // Optional: limit biar ringan
            laporanAdapter.updateList(filteredList)
            // laporanAdapter.updateList(filteredList.take(20))
        }
    }

    private fun tampilkanDetailLaporan(laporan: LaporanModel) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detail Laporan")
            .setMessage(
                "Barang: ${laporan.nama_barang}\n" +
                        "Villa: ${laporan.villa_nama}\n" +
                        "Status: ${laporan.status}"
            )
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Update Status") { _, _ ->

                val opsi = arrayOf("belum_ditindaklanjuti", "proses", "selesai")

                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Status Baru")
                    .setItems(opsi) { _, i ->
                        viewModel.updateStatusLaporan(laporan.id, opsi[i]) { sukses ->
                            if (sukses) {
                                Toast.makeText(requireContext(), "Status diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}