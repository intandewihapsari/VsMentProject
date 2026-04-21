package com.indri.vsmentproject.ui.manager.template

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.indri.vsmentproject.databinding.FragmentTemplateListBinding

class TemplateListFragment : Fragment() {

    private var _binding: FragmentTemplateListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.fabAddTemplate.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as TemplateManager)
                    .findViewById<View>(android.R.id.content).id,
                    FragmentTemplateForm()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}