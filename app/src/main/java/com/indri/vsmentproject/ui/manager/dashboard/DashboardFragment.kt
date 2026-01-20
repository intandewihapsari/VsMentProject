package com.indri.vsmentproject.ui.manager.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.ui.manager.task.TugasFragment
import com.indri.vsmentproject.ui.manager.report.LaporanFragment // Sesuaikan jika ada fragment notifikasi khusus

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter
    private val auth = FirebaseAuth.getInstance()

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
        observeDashboardData()

        val currentUid = auth.currentUser?.uid
        currentUid?.let {
            viewModel.setManagerUid(it)
        } ?: run {
            Toast.makeText(requireContext(), "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            items = emptyList(),
            onTambahTugasClick = {
                // PINDAH KE FRAGMENT TUGAS
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, TugasFragment()) // Pastikan ID container benar
                    .addToBackStack(null)
                    .commit()
            },
            onKirimNotifClick = {
                // PINDAH KE FRAGMENT LAPORAN/NOTIFIKASI
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, LaporanFragment())
                    .addToBackStack(null)
                    .commit()
            },
            onTugasClick = { tugas ->
                // DIALOG DETAIL TUGAS
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Detail Tugas")
                    .setMessage("Tugas: ${tugas.tugas}\nStatus: ${tugas.status}\nPetugas: ${tugas.worker_name}\nLokasi: ${tugas.villa_nama} - ${tugas.ruangan}")
                    .setPositiveButton("Tutup", null)
                    .show()
            },
            onReloadAnalisisClick = {
                val uid = auth.currentUser?.uid
                uid?.let {
                    viewModel.setManagerUid(it)
                    Toast.makeText(requireContext(), "Data diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvDashboard.apply {
            adapter = dashboardAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeDashboardData() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Opsional: binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    resource.data?.let { dashboardAdapter.updateData(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}