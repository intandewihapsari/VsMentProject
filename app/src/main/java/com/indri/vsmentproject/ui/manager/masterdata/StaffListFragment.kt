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
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.databinding.FragmentStaffListBinding
import com.indri.vsmentproject.ui.manager.report.LaporanAdapter
class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = LaporanAdapter { laporan ->
            val staff = viewModel.staffList.value?.find { it.uid == laporan.id }
            staff?.let { showMenuOpsiStaff(it) }
        }

        binding.rvStaff.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStaff.adapter = adapter

        viewModel.getData()

        viewModel.staffList.observe(viewLifecycleOwner) { list ->
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
    }

    private fun showMenuOpsiStaff(staff: UserModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(staff.nama)
            .setItems(arrayOf("Hapus Staff")) { _, _ ->
                viewModel.hapusStaff(staff.uid)
            }
            .show()
    }
}