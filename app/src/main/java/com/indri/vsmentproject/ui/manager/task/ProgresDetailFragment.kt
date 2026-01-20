package com.indri.vsmentproject.ui.manager.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.databinding.FragmentProgresDetailBinding

class ProgresDetailFragment : Fragment() {

    private var _binding: FragmentProgresDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TugasViewModel by viewModels()
    private lateinit var progresAdapter: ProgresVillaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgresDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        progresAdapter = ProgresVillaAdapter()
        binding.rvProgresDetail.apply {
            adapter = progresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Ambil data grouped dari ViewModel
        viewModel.getTugasGroupedByVilla()
        viewModel.tugasGrouped.observe(viewLifecycleOwner) { groups ->
            if (groups != null) {
                progresAdapter.setList(groups)
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}