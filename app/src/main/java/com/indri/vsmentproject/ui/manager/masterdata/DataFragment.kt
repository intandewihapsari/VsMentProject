package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentDataBinding
import com.indri.vsmentproject.ui.manager.report.LaporanAdapter

class DataFragment : Fragment() {

    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapterRiwayat: LaporanAdapter

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

        // Ambil UID Manager untuk melihat riwayat instruksi yang pernah dikirim
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.getRiwayatInstruksi(managerUid)
    }

    private fun setupRecyclerView() {
        adapterRiwayat = LaporanAdapter { laporan ->
            // Aksi saat item riwayat diklik (opsional)
        }

        binding.rvRiwayatNotif.apply {
            adapter = adapterRiwayat
            layoutManager = LinearLayoutManager(requireContext())
            // Matikan nested scrolling agar scroll lancar di dalam NestedScrollView
            isNestedScrollingEnabled = false
        }
    }

    private fun setupNavigation() {
        // Navigasi ke Kelola Villa
        binding.btnManageVilla.setOnClickListener {
            navigasiKe(VillaListFragment())
        }

        // Navigasi ke Kelola Staff
        binding.btnManageStaff.setOnClickListener {
            navigasiKe(StaffListFragment())
        }
    }

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