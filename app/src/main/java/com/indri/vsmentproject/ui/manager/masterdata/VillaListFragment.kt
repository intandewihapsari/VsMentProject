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
import com.indri.vsmentproject.data.model.villa.VillaModel
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

        // Setup Adapter dengan listener klik untuk menu Edit/Hapus
        val adapter = PilihVillaAdapter { villa ->
            showMenuOpsi(villa)
        }

        binding.rvVilla.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        // Observasi Data
        viewModel.getData()
        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        // Navigasi ke Fragment Tambah Villa
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
        val opsi = arrayOf("Edit Detail Villa", "Hapus Villa")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(villa.nama)
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> {
                        // Navigasi ke TambahVillaFragment dengan membawa data (Mode Edit)
                        val fragment = TambahVillaFragment.newInstance(villa)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    1 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Hapus Villa")
                            .setMessage("Apakah Anda yakin ingin menghapus ${villa.nama}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.hapusVilla(villa.id)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}