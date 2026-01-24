package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.databinding.FragmentStaffListBinding
import com.indri.vsmentproject.ui.manager.report.LaporanAdapter

class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LaporanAdapter { laporan ->
            // Cari data staff asli berdasarkan ID dari laporan
            val staff = viewModel.staffList.value?.find { it.uid == laporan.id }
            staff?.let { showMenuOpsiStaff(it) }
        }

        binding.rvStaff.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        viewModel.getData()
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            // Mapping StaffModel ke LaporanModel agar bisa tampil di adapter
            val mapping = list.map {
                LaporanModel(
                    id = it.uid,
                    nama_barang = it.nama,
                    tipe_laporan = it.posisi,
                    status = it.status.uppercase()
                )
            }
            adapter.updateList(mapping)
        }

        binding.btnTambahStaff.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TambahStaffFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showMenuOpsiStaff(staff: StaffModel) {
        val opsi = arrayOf("Hapus Akses Staff")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(staff.nama)
            .setItems(opsi) { _, _ ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus Staff")
                    .setMessage("Hapus data ${staff.nama}?")
                    .setPositiveButton("Ya") { _, _ ->
                        viewModel.hapusStaff(staff.uid)
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}