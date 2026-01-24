package com.indri.vsmentproject.ui.manager.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.ui.manager.task.TugasFragment
import com.indri.vsmentproject.ui.manager.report.LaporanFragment

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupNotifikasiLogic()
        observeDashboardData()

        auth.currentUser?.uid?.let {
            viewModel.setManagerUid(it)
        }
    }


    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            items = emptyList(),
            onTambahTugasClick = {
                // Navigasi ke TugasFragment dengan flag auto-input
                val fragment = TugasFragment().apply {
                    arguments = Bundle().apply { putBoolean("BUKA_INPUT_OTOMATIS", true) }
                }
                navigasiKe(fragment)
            },
            onKirimNotifClick = {
                // Munculkan overlay form notifikasi
                binding.layoutFormKirimNotifikasi.root.visibility = View.VISIBLE
                loadVillaToSpinner()
            },
            onTugasClick = { tugas ->
                // Jika klik tugas urgent/pending, langsung ke Laporan
                navigasiKe(LaporanFragment())
            },
            onReloadAnalisisClick = {
                auth.currentUser?.uid?.let { viewModel.setManagerUid(it) }
            }
        )

        binding.rvDashboard.apply {
            adapter = dashboardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupNotifikasiLogic() {
        val form = binding.layoutFormKirimNotifikasi

        // Tombol Tutup
        form.btnCloseNotif.setOnClickListener {
            form.root.visibility = View.GONE
        }

        // Tombol Kirim Firebase
        form.btnKirimNotifFirebase.setOnClickListener {
            val judul = form.etJudulNotif.text.toString().trim()
            val pesan = form.etPesanNotif.text.toString().trim()
            val villaTerpilih = form.spinnerVillaNotif.selectedItem?.toString() ?: ""
            val isUrgent = form.switchUrgent.isChecked

            if (judul.isNotEmpty() && pesan.isNotEmpty()) {
                // Kirim data ke ViewModel
                // viewModel.kirimNotifikasi(villaTerpilih, judul, pesan, isUrgent)

                Toast.makeText(requireContext(), "Notifikasi $judul Berhasil Dikirim!", Toast.LENGTH_SHORT).show()
                form.root.visibility = View.GONE

                // Reset Form
                form.etJudulNotif.text?.clear()
                form.etPesanNotif.text?.clear()
                form.switchUrgent.isChecked = false
            } else {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadVillaToSpinner() {
        if (!isAdded) return

        viewModel.villaList.observe(viewLifecycleOwner) { list ->
            activity?.let { context ->
                if (!list.isNullOrEmpty()) {
                    val namaVilla = list.map { it.nama ?: "Villa Tanpa Nama" }

                    val spinnerAdapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item, // Layout item spinner
                        namaVilla
                    )
                    // Layout dropdown saat diklik
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    binding.layoutFormKirimNotifikasi.spinnerVillaNotif.apply {
                        adapter = spinnerAdapter
                        // Memastikan spinner bisa diklik
                        isEnabled = true
                        isClickable = true
                    }
                } else {
                    Toast.makeText(requireContext(), "Data Villa Kosong", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getVillaList()
    }

    private fun observeDashboardData() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Progress Bar jika perlu */ }
                is Resource.Success -> {
                    if (resource.data.isNullOrEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        dashboardAdapter.updateData(resource.data!!)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Gagal memuat data: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigasiKe(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}