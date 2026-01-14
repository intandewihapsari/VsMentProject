package com.indri.vsmentproject.ui.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.data.model.LaporanModel
import com.indri.vsmentproject.R
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

        viewModel.getLaporanList()
    }

    private fun setupRecyclerView() {
        laporanAdapter = LaporanAdapter { laporan -> tampilkanDetailLaporan(laporan) }
        binding.rvLaporan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = laporanAdapter
        }
    }

    private fun observeData() {
        viewModel.laporanList.observe(viewLifecycleOwner) { list ->
            setupSpinnerVilla(list)
            applyFilter(list)
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
                applyFilter(viewModel.laporanList.value ?: emptyList())
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
                applyFilter(viewModel.laporanList.value ?: emptyList())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun applyFilter(list: List<LaporanModel>) {
        var filteredList = list

        // Filter Status
        if (currentFilterStatus != "semua") {
            filteredList = filteredList.filter { it.status_laporan == currentFilterStatus }
        }

        // Filter Villa
        if (currentFilterVilla != "Semua Villa") {
            filteredList = filteredList.filter { it.villa_nama == currentFilterVilla }
        }

        laporanAdapter.updateList(filteredList)
    }

    private fun tampilkanDetailLaporan(laporan: LaporanModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Detail Laporan")
            .setMessage("Barang: ${laporan.nama_barang}\nVilla: ${laporan.villa_nama}\nStatus: ${laporan.status_laporan}")
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