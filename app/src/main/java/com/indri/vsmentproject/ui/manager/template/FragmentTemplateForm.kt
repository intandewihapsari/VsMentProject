package com.indri.vsmentproject.ui.manager.template

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.databinding.FragmentTemplateFormBinding

class FragmentTemplateForm : Fragment() {

    private var _binding: FragmentTemplateFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplateFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnSimpanTemplate.setOnClickListener {
            simpanTemplate()
        }
    }

    private fun simpanTemplate() {

        val name = binding.etNamaTemplate.text.toString().trim()
        val rawTasks = binding.etListTask.text.toString().trim()

        if (name.isEmpty() || rawTasks.isEmpty()) {
            Toast.makeText(requireContext(), "Isi data lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        val tasks = rawTasks.split("\n").filter { it.isNotBlank() }

        val template = TaskTemplateModel(
            name = name,
            kategori = "General",
            tasks = tasks
        )

        viewModel.createTemplate(template) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Template tersimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}