package com.indri.vsmentproject.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Di dalam onViewCreated DashboardFragment.kt
        dashboardAdapter = DashboardAdapter(
            onTambahTugasClick = {
                binding.rvDashboard.visibility = View.GONE
                binding.layoutFormTambahTugas.root.visibility = View.VISIBLE
            },
            onKirimNotifClick = {
                binding.rvDashboard.visibility = View.GONE
                binding.layoutFormKirimNotifikasi.root.visibility = View.VISIBLE
            },
            onTugasClick = { tugas ->
                // APA YANG TERJADI SAAT TUGAS DIKLIK
                android.widget.Toast.makeText(requireContext(), "Klik: ${tugas.tugas}", android.widget.Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvDashboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dashboardAdapter
        }

        viewModel.combinedDashboardData.observe(viewLifecycleOwner) { items ->
            dashboardAdapter.updateData(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}