package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.databinding.FragmentProgresDetailBinding
import com.indri.vsmentproject.ui.manager.task.TugasViewModel

class ProgresDetailFragment : Fragment() {

    private var _binding: FragmentProgresDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TugasViewModel by viewModels()
    private lateinit var progresAdapter: ProgresVillaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgresDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        loadData()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // 🔥 Setup RecyclerView
    private fun setupRecyclerView() {
        progresAdapter = ProgresVillaAdapter()

        binding.rvProgresDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = progresAdapter
        }
    }

    // 🔥 Observe data dari ViewModel
    private fun observeData() {
        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { dataRekap ->

            if (!dataRekap.isNullOrEmpty()) {
                progresAdapter.setList(dataRekap)
            } else {
                // Optional: bisa kasih empty state di sini
                progresAdapter.setList(emptyList())
            }
        }
    }

    // 🔥 Ambil data dari Firebase via ViewModel
    private fun loadData() {
        viewModel.getTugasGroupedByVilla()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}