package com.indri.vsmentproject.ui.manager.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
// Import diarahkan ke package lokal dashboard
import com.indri.vsmentproject.ui.manager.dashboard.DashboardAdapter
import com.indri.vsmentproject.ui.manager.dashboard.DashboardViewModel

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel secara otomatis
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        // Ambil UID manager yang sedang login secara dinamis
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.setManagerUid(currentUid)
    }

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            onTambahTugasClick = {
                // Navigasi ke form tambah tugas
                Toast.makeText(context, "Membuka Form Tambah Tugas", Toast.LENGTH_SHORT).show()
            },
            onKirimNotifClick = {
                // Navigasi ke form kirim notifikasi
                Toast.makeText(context, "Membuka Form Notifikasi", Toast.LENGTH_SHORT).show()
            },
            onTugasClick = { tugas ->
                // PERBAIKAN: Gunakan field 'tugas' sesuai TugasModel kamu
                Toast.makeText(context, "Detail: ${tugas.tugas}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvDashboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dashboardAdapter
        }
    }

    private fun observeData() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Opsional: Tampilkan progress bar utama
                }
                is Resource.Success -> {
                    if (resource.data.isNullOrEmpty()) {
                        binding.rvDashboard.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvDashboard.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                        dashboardAdapter.updateData(resource.data!!)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}