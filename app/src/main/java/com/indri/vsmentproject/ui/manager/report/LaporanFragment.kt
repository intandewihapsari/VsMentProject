package com.indri.vsmentproject.ui.manager.report

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentLaporanBinding

class LaporanFragment : Fragment() {
    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LaporanViewModel by viewModels()
    private lateinit var laporanAdapter: LaporanAdapter

    private var currentFilterStatus = "semua"
    private var currentFilterVilla = "Semua Villa"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        laporanAdapter = LaporanAdapter { laporan -> tampilkanDetailLaporan(laporan) }
        binding.rvLaporan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = laporanAdapter
        }
    }

    private fun observeData() {
        viewModel.laporanResource.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Opsional: Tampilkan ProgressBar */ }
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
                    R.id.btnBelum -> "belum_ditindaklanjuti"
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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, villas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterVilla.adapter = adapter
        binding.spinnerFilterVilla.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                currentFilterVilla = villas[pos]
                applyFilter(viewModel.laporanResource.value?.data ?: emptyList())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun applyFilter(list: List<LaporanModel>) {
        var filteredList = list

        // 1. Filter berdasarkan Status
        if (currentFilterStatus != "semua") {
            filteredList = filteredList.filter { it.status == currentFilterStatus }
        }

        // 2. Filter berdasarkan Villa
        if (currentFilterVilla != "Semua Villa") {
            filteredList = filteredList.filter { it.villa_nama == currentFilterVilla }
        }

        // 3. Update Adapter dan Handle Tampilan Kosong
        if (filteredList.isEmpty()) {
            binding.rvLaporan.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE // ID ini sudah ada di XML tadi
        } else {
            binding.rvLaporan.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            laporanAdapter.updateList(filteredList)
        }
    }

    private fun tampilkanDetailLaporan(laporan: LaporanModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Detail Laporan")
            .setMessage("Barang: ${laporan.nama_barang}\nVilla: ${laporan.villa_nama}\nStatus: ${laporan.status}")
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Ubah Status") { _, _ ->
                val opsi = arrayOf("belum_ditindaklanjuti", "proses", "selesai")
                AlertDialog.Builder(requireContext()).setItems(opsi) { _, i ->
                    viewModel.updateStatusLaporan(laporan.id, opsi[i]) { sukses ->
                        if (sukses) Toast.makeText(requireContext(), "Berhasil Update", Toast.LENGTH_SHORT).show()
                    }
                }.show()
            }.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}