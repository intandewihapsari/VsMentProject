package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentVillaListBinding
import com.indri.vsmentproject.ui.manager.task.PilihVillaAdapter

class VillaListFragment : Fragment() {

    private var _binding: FragmentVillaListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVillaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PilihVillaAdapter { villa ->
            showMenuOpsi(villa)
        }

        binding.rvVilla.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.getData(managerUid)

        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            binding.progressBar.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                binding.layoutEmptyVilla.visibility = View.VISIBLE
                binding.rvVilla.visibility = View.GONE
            } else {
                binding.layoutEmptyVilla.visibility = View.GONE
                binding.rvVilla.visibility = View.VISIBLE
                adapter.updateData(list)
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

        binding.btnTambahVilla.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TambahVillaFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showMenuOpsi(villa: VillaModel) {
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val opsi = arrayOf("Edit Detail Villa", "Hapus Villa")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(villa.nama)
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> {
                        val fragment = TambahVillaFragment.newInstance(villa)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    1 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Hapus Data")
                            .setMessage("Apakah Anda yakin ingin menghapus ${villa.nama}? Tindakan ini tidak dapat dibatalkan.")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.hapusVilla(managerUid, villa.id)
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
