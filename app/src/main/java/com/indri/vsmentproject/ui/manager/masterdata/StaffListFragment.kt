package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentStaffListBinding

class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()
    private lateinit var staffAdapter: StaffAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (managerUid.isNotEmpty()) {
            viewModel.getData(managerUid)
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

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter { staff ->
            showMenuOpsiStaff(staff)
        }

        binding.rvStaff.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = staffAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            binding.progressBar.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                binding.layoutEmptyStaff.visibility = View.VISIBLE
                binding.rvStaff.visibility = View.GONE
            } else {
                binding.layoutEmptyStaff.visibility = View.GONE
                binding.rvStaff.visibility = View.VISIBLE
                staffAdapter.submitList(list)
            }
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMenuOpsiStaff(staff: UserModel) {
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                            .setTitle("Hapus Data")
                            .setMessage("Apakah Anda yakin ingin menghapus ${staff.nama}? Tindakan ini tidak dapat dibatalkan.")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.hapusStaff(managerUid, staff.uid)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
