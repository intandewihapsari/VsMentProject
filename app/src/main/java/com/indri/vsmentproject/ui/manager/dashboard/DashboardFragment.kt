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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentDashboardBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.manager.report.LaporanFragment
import com.indri.vsmentproject.ui.manager.task.TugasFragment

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dashboardAdapter: DashboardAdapter
    private val auth = FirebaseAuth.getInstance()

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

        setupRecyclerView()
        setupNotifikasiLogic()
        observeDashboardData()
        observeKirimNotifikasi()

        auth.currentUser?.uid?.let {
            viewModel.setManagerUid(it)
        }
    }

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            onTambahTugasClick = {
                (activity as? ManagerActivity)?.let { managerActivity ->
                    // 1. Pindahkan dulu status Tab BottomNavigationView ke Tugas
                    managerActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                        ?.selectedItemId = R.id.navigation_tugas

                    // 2. Buat fragment dengan argumen OPEN_ADD_TASK = true
                    val fragment = TugasFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("OPEN_ADD_TASK", true)
                        }
                    }

                    // 3. Timpa container dengan fragment berpeta argumen ini
                    managerActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit()
                }
            },
            onKirimNotifClick = {
                binding.layoutFormKirimNotifikasi.root.visibility = View.VISIBLE
                loadVillaToSpinner()
                loadStaffToSpinner()
            },
            onEditTugas = { _ ->
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

        form.btnCloseNotif.setOnClickListener {
            form.root.visibility = View.GONE
        }

        form.btnKirimNotifFirebase.setOnClickListener {
            val judul = form.etJudulNotif.text.toString().trim()
            val pesan = form.etPesanNotif.text.toString().trim()
            val villaTerpilih = form.spinnerVillaNotif.selectedItem?.toString() ?: ""
            val isUrgent = form.switchUrgent.isChecked
            val managerUid = auth.currentUser?.uid ?: ""

            if (judul.isNotEmpty() && pesan.isNotEmpty() && managerUid.isNotEmpty()) {
                // 🔥 PANGGIL VIEWMODEL UNTUK SIMPAN KE FIREBASE
                viewModel.kirimNotifikasi(
                    managerUid = managerUid,
                    villaNama = villaTerpilih,
                    judul = judul,
                    pesan = pesan,
                    isUrgent = isUrgent
                )
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
                        android.R.layout.simple_spinner_item,
                        namaVilla
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }

                    binding.layoutFormKirimNotifikasi.spinnerVillaNotif.apply {
                        adapter = spinnerAdapter
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
    private fun observeKirimNotifikasi() {
        viewModel.kirimNotifStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Opsional: Bisa tampilkan progress jika mau
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Notifikasi berhasil dikirim ke Firebase!", Toast.LENGTH_SHORT).show()

                    // Tutup & Reset Form
                    binding.layoutFormKirimNotifikasi.apply {
                        root.visibility = View.GONE
                        etJudulNotif.text?.clear()
                        etPesanNotif.text?.clear()
                        switchUrgent.isChecked = false
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Gagal: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadStaffToSpinner() {
        if (!isAdded) return

        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            activity?.let { context ->
                if (!list.isNullOrEmpty()) {
                    val namaStaff = list.map { it.nama ?: "Staff Tanpa Nama" }
                    val spinnerAdapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item,
                        namaStaff
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }

                    binding.layoutFormKirimNotifikasi.spinnerStaffNotif.apply {
                        adapter = spinnerAdapter
                        isEnabled = true
                        isClickable = true
                    }
                } else {
                    Toast.makeText(requireContext(), "Data Staff Kosong", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getStaffList()
    }

    private fun observeDashboardData() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Progress Bar jika diperlukan */ }
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
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }


    // =========================================================
// HELPER FOR BACK NAVIGATION (DIPANGGIL OLEH MANAGERACTIVITY)
// =========================================================

    /**
     * Mengecek apakah form/overlay Kirim Notifikasi sedang terbuka.
     */
    fun isNotifOverlayOpen(): Boolean {
        return _binding != null && binding.layoutFormKirimNotifikasi.root.visibility == View.VISIBLE
    }

    /**
     * Menyembunyikan/menutup form Kirim Notifikasi.
     */
    fun closeNotifOverlay() {
        if (_binding != null) {
            binding.layoutFormKirimNotifikasi.root.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}