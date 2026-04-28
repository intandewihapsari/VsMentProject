package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.databinding.FragmentProgresDetailBinding
import com.indri.vsmentproject.ui.manager.task.TugasViewModel

class ProgresDetailFragment : Fragment() {

    private var _binding: FragmentProgresDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TugasViewModel
    private lateinit var adapter: ProgresVillaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgresDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(this)[TugasViewModel::class.java]
        adapter = ProgresVillaAdapter()

        binding.rvProgresDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProgresDetail.adapter = adapter

        viewModel.rawGroupsLive.observe(viewLifecycleOwner) {
            adapter.setList(it ?: emptyList())
        }

        viewModel.getTugasGroupedByVilla()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}