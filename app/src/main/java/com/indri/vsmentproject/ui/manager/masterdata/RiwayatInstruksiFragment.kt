package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.databinding.FragmentRiwayatInstruksiBinding
import com.indri.vsmentproject.ui.manager.report.LaporanAdapter

class RiwayatInstruksiFragment : Fragment() {
    private var _binding: FragmentRiwayatInstruksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatInstruksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LaporanAdapter { /* Aksi klik riwayat */ }
        binding.rvRiwayatInstruksi.adapter = adapter
        binding.rvRiwayatInstruksi.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.getRiwayatInstruksi(uid)

        viewModel.riwayatNotif.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                adapter.updateList(list)
                binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}