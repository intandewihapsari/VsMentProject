package com.indri.vsmentproject.UI.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.Data.Model.LaporanModel
import com.indri.vsmentproject.databinding.FragmentLaporanBinding

class LaporanFragment : Fragment() {
    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LaporanViewModel by viewModels()
    private lateinit var laporanAdapter: LaporanAdapter // Pastikan buat adapternya juga

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        laporanAdapter = LaporanAdapter { laporan ->
            tampilkanDetailLaporan(laporan)
        }

        binding.rvLaporan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = laporanAdapter
        }

        viewModel.laporanList.observe(viewLifecycleOwner) { list ->
            laporanAdapter.updateList(list)
        }

        viewModel.getLaporanList()
    }

    private fun tampilkanDetailLaporan(laporan: LaporanModel) {
        val detailMsg = """
            Villa: ${laporan.villa_nama}
            Area: ${laporan.area}
            Barang: ${laporan.nama_barang}
            Jenis: ${laporan.jenis_laporan.uppercase()}
            Pelapor: ${laporan.staff_nama}
            Waktu: ${laporan.waktu_lapor}
            Keterangan: ${laporan.keterangan}
            Status: ${laporan.status_laporan.replace("_", " ")}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Detail Laporan")
            .setMessage(detailMsg)
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Ubah Status") { _, _ ->
                pilihStatusBaru(laporan)
            }
            .show()
    }

    private fun pilihStatusBaru(laporan: LaporanModel) {
        val opsiStatus = arrayOf("belum_ditindaklanjuti", "proses", "selesai")

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Status Baru")
            .setItems(opsiStatus) { _, which ->
                val statusTerpilih = opsiStatus[which]
                viewModel.updateStatusLaporan(laporan.id, statusTerpilih) { sukses ->
                    if (sukses) Toast.makeText(requireContext(), "Status diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}