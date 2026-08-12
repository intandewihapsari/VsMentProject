package com.indri.vsmentproject.ui.manager.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentTemplateFormBinding

class FragmentTemplateForm : Fragment() {

    private var _binding: FragmentTemplateFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateViewModel by viewModels()
    private val itemList = mutableListOf<String>()
    private var currentManagerId = ""
    private var currentTemplateId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTemplateFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentManagerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupListeners()
        observeViewModel()
        checkEditMode()
    }

    private fun checkEditMode() {
        val template = arguments?.getParcelable<TaskTemplateModel>("EXTRA_TEMPLATE")
        if (template != null) {
            currentTemplateId = template.id
            binding.etNamaTemplate.setText(template.nama_template)
            binding.etDeskripsiTemplate.setText(template.deskripsi)
            itemList.clear()
            itemList.addAll(template.list_tugas_item)
            renderItemsText()
            binding.btnSimpanTemplate.text = "Perbarui Template"
        }
    }

    private fun setupListeners() {
        binding.btnTambahItem.setOnClickListener {
            val itemTugas = binding.etItemTugas.text.toString().trim()
            if (itemTugas.isNotEmpty()) {
                itemList.add(itemTugas)
                binding.etItemTugas.setText("")
                renderItemsText()
            } else {
                Toast.makeText(context, "Ketik item tugas terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSimpanTemplate.setOnClickListener {
            val nama = binding.etNamaTemplate.text.toString().trim()
            val deskripsi = binding.etDeskripsiTemplate.text.toString().trim()

            if (nama.isEmpty() || itemList.isEmpty()) {
                Toast.makeText(context, "Nama template dan minimal 1 item tugas harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val templateBaru = TaskTemplateModel(
                id = currentTemplateId ?: "",
                nama_template = nama,
                deskripsi = deskripsi,
                list_tugas_item = itemList
            )

            viewModel.saveTemplate(currentManagerId, templateBaru)
        }

        binding.tvPreviewItems.setOnClickListener {
            if (itemList.isNotEmpty()) {
                itemList.removeAt(itemList.size - 1)
                renderItemsText()
                Toast.makeText(context, "Item terakhir dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderItemsText() {
        binding.tvPreviewItems.text = itemList.joinToString(separator = "\n") { "• $it" }
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Template Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}