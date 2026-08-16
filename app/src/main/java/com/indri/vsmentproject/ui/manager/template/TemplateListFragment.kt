package com.indri.vsmentproject.ui.manager.template

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
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentTemplateListBinding

class TemplateListFragment : Fragment() {

    private var _binding: FragmentTemplateListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateViewModel by viewModels()
    private lateinit var adapter: TemplateAdapter
    private var currentManagerId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTemplateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentManagerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupRecyclerView()
        observeViewModel()

        binding.fabTambahTemplate.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTemplateForm())
                .addToBackStack(null)
                .commit()
        }

        binding.btnTambahPertama.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTemplateForm())
                .addToBackStack(null)
                .commit()
        }

        viewModel.fetchTemplates(currentManagerId)
        viewModel.fetchVillasAndStaffs(currentManagerId)
    }

    private fun setupRecyclerView() {
        adapter = TemplateAdapter(
            onApplyClick = { template ->
                showApplyDialog(template)
            },
            onEditClick = { template ->
                val fragment = FragmentTemplateForm().apply {
                    arguments = Bundle().apply {
                        putParcelable("EXTRA_TEMPLATE", template)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { template ->
                showKonfirmasiHapusTemplate(template)
            }
        )
        binding.rvTemplate.layoutManager = LinearLayoutManager(context)
        binding.rvTemplate.adapter = adapter
    }

    private fun showKonfirmasiHapusTemplate(template: TaskTemplateModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus ${template.nama_template}? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteTemplate(currentManagerId, template.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showApplyDialog(template: TaskTemplateModel) {
        val villas = viewModel.villaList.value ?: emptyList()
        val staffs = viewModel.staffList.value ?: emptyList()

        if (villas.isEmpty() || staffs.isEmpty()) {
            Toast.makeText(context, "Data Villa atau Staff belum siap, mohon tunggu...", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = DialogApplyTemplate(template, villas, staffs) { villaId, villaNama, ruangan, selectedStaffs, deadline ->
            viewModel.applyTemplate(
                managerId = currentManagerId,
                villaId = villaId,
                villaNama = villaNama,
                ruanganNama = ruangan,
                selectedStaffs = selectedStaffs,
                template = template,
                deadline = deadline
            )
        }
        dialog.show(parentFragmentManager, "DialogApplyTemplate")
    }

    private fun observeViewModel() {
        viewModel.templateList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = resource.data ?: emptyList()
                    if (data.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvTemplate.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvTemplate.visibility = View.VISIBLE
                        adapter.setData(data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.applyStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> Toast.makeText(context, "Menerapkan template...", Toast.LENGTH_SHORT).show()
                is Resource.Success -> Toast.makeText(context, "Berhasil menerapkan template!", Toast.LENGTH_LONG).show()
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
