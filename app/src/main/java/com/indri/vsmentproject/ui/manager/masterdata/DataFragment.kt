package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentDataBinding
import com.indri.vsmentproject.ui.manager.template.FragmentTemplateForm

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

        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (managerUid.isNotEmpty()) {
            viewModel.getRiwayatInstruksi(managerUid)
        }
    }

    // =========================
    // RECYCLER
    // =========================
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
            // Pindah ke Fragment Template yang kamu buat tadi
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTemplateForm())
                //.addToAddBackStack(null)
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

    // =========================
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