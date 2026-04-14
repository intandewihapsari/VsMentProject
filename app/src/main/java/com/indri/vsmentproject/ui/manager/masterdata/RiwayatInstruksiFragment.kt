package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.databinding.FragmentRiwayatInstruksiBinding

class RiwayatInstruksiFragment : Fragment() {

    private var _binding: FragmentRiwayatInstruksiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: NotifikasiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatInstruksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadData()
        observeData()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // =========================
    // SETUP RECYCLER
    // =========================
    private fun setupRecycler() {
        adapter = NotifikasiAdapter { notif ->
            // optional klik item
        }

        binding.rvRiwayatInstruksi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RiwayatInstruksiFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    // =========================
    // LOAD DATA
    // =========================
    private fun loadData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            viewModel.getRiwayatInstruksi(uid)
        }
    }

    // =========================
    // OBSERVE DATA
    // =========================
    private fun observeData() {
        viewModel.riwayatNotif.observe(viewLifecycleOwner) { list ->

            adapter.updateList(list)

            binding.layoutEmpty.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // =========================
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}