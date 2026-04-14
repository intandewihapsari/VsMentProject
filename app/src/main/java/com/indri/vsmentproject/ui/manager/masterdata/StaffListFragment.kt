package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.databinding.FragmentStaffListBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.manager.report.LaporanAdapter
class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: StaffAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 🔥 ADAPTER GRID STAFF
        adapter = StaffAdapter { staff ->
            showMenuOpsiStaff(staff)
        }

        binding.rvStaff.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@StaffListFragment.adapter
        }

        // 🔥 LOAD DATA
        viewModel.getData()

        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // 🔥 BUTTON TAMBAH STAFF
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

    // 🔥 MENU EDIT + DELETE
    private fun showMenuOpsiStaff(staff: UserModel) {

        val opsi = arrayOf("Edit Staff", "Hapus Staff")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(staff.nama)
            .setItems(opsi) { _, which ->

                when (which) {

                    0 -> {
                        val fragment = TambahStaffFragment.newInstance(staff)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    1 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Hapus Staff")
                            .setMessage("Hapus ${staff.nama}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.hapusStaff(staff.uid)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }
            .show()
    }
    override fun onResume() {
        super.onResume()
        (activity as? ManagerActivity)?.setHeaderVisible(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}