package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
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

        // Listener Filter
        binding.toggleGroupStatus.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val filterType = when (checkedId) {
                    R.id.btnBelum -> "Pending"
                    R.id.btnSelesai -> "Selesai"
                    else -> "Semua"
                }
                viewModel.filterNotif(filterType)
            }
        }


        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecycler() {
        adapter = NotifikasiAdapter { notif ->
            // Klik detail jika perlu
        }
        binding.rvRiwayatInstruksi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RiwayatInstruksiFragment.adapter
        }
    }

    private fun loadData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            viewModel.getRiwayatInstruksi(uid)
        }
    }

    private fun observeData() {
        viewModel.riwayatNotif.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)

            // Atur visibility
            if (list.isNullOrEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvRiwayatInstruksi.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvRiwayatInstruksi.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}