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
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
import com.indri.vsmentproject.data.utils.Resource

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

        // Pemicu pertama: Ambil UID Manager yang sedang login
        val currentUid = auth.currentUser?.uid
        currentUid?.let {
            viewModel.setManagerUid(it)
        } ?: run {
            Toast.makeText(requireContext(), "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi Adapter dengan 5 Parameter Lengkap
        dashboardAdapter = DashboardAdapter(
            items = emptyList(),
            onTambahTugasClick = {
                // Navigasi ke Tab Tugas (Contoh pindah tab atau buka dialog)
                Toast.makeText(requireContext(), "Buka Form Tambah Tugas", Toast.LENGTH_SHORT).show()
            },
            onKirimNotifClick = {
                // Aksi tombol kirim notifikasi manual
                Toast.makeText(requireContext(), "Fitur Kirim Notifikasi", Toast.LENGTH_SHORT).show()
            },
            onTugasClick = { tugas ->
                // Menampilkan detail tugas saat item di list diklik
                Toast.makeText(requireContext(), "Tugas: ${tugas.tugas}", Toast.LENGTH_SHORT).show()
            },
            onReloadAnalisisClick = {
                // FUNGSI RELOAD: Paksa ViewModel hitung ulang summary dari database
                val uid = auth.currentUser?.uid
                uid?.let {
                    viewModel.setManagerUid(it)
                    Toast.makeText(requireContext(), "Memperbarui data...", Toast.LENGTH_SHORT).show()
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
                    // Tampilkan Shimmer atau Progress (Jika ada)
                }
                is Resource.Success -> {
                    // Update data ke adapter secara dinamis
                    resource.data?.let { dashboardAdapter.updateData(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Gagal memuat data: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}