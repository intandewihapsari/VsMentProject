package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentDataBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.manager.template.TemplateListFragment

class DataFragment : Fragment() {

    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapterRiwayat: NotifikasiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupNavigation()
        observeRiwayat()

        // ➕ 1. DIPANGGIL DI SINI
        setupBackNavigation()

        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (managerUid.isNotEmpty()) {
            viewModel.getRiwayatInstruksi(managerUid)
        }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Jika ada sub-fragment di BackStack (seperti VillaList/StaffList/Template)
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack() // Tutup sub-fragment
                    } else {
                        // Jika sudah di DataFragment utama, paksa pindah ke Tab Home
                        (activity as? ManagerActivity)?.let { managerActivity ->
                            managerActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                                ?.selectedItemId = R.id.navigation_home
                        }
                    }
                }
            }
        )
    }

    private fun setupRecyclerView() {
        adapterRiwayat = NotifikasiAdapter { notif ->
            // klik item opsional
        }

        binding.rvRiwayatNotif.apply {
            adapter = adapterRiwayat
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.btnInstructionTemplate.setOnClickListener {
            // Pindah ke Fragment Template
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TemplateListFragment())
                .addToBackStack(null) // 💡 Disarankan pakai addToBackStack agar bisa di-pop
                .commit()
        }
    }

    // =========================
    // NAVIGATION
    // =========================
    private fun setupNavigation() {
        binding.btnManageVilla.setOnClickListener {
            navigasiKe(VillaListFragment())
        }

        binding.btnManageStaff.setOnClickListener {
            navigasiKe(StaffListFragment())
        }
    }

    // =========================
    // OBSERVER
    // =========================
    private fun observeRiwayat() {
        viewModel.riwayatNotif.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.tvEmptyNotif.visibility = View.VISIBLE
                binding.rvRiwayatNotif.visibility = View.GONE
            } else {
                binding.tvEmptyNotif.visibility = View.GONE
                binding.rvRiwayatNotif.visibility = View.VISIBLE
                adapterRiwayat.updateList(list)
            }
        }
    }

    private fun navigasiKe(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}