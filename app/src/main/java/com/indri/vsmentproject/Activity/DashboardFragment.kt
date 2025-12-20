package com.indri.vsmentproject.Activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.DashboardAdapter
import com.indri.vsmentproject.DashboardItem
import com.indri.vsmentproject.ViewModel.DashboardViewModel
import com.indri.vsmentproject.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardAdapter: DashboardAdapter
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardAdapter = DashboardAdapter()
        binding.rvDashboard.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvDashboard.adapter = dashboardAdapter

        viewModel.notifikasiUrgent.observe(viewLifecycleOwner) { list ->
            val dashboardItems = listOf(
                DashboardItem.NotifikasiUrgent(list),
                DashboardItem.AnalisisCepat(emptyList()),

            )
            dashboardAdapter.update(dashboardItems as List<DashboardItem>)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
