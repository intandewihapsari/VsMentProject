package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.databinding.FragmentProgresDetailBinding
import com.indri.vsmentproject.ui.manager.task.TugasViewModel

class ProgresDetailFragment : Fragment() {

    private var _binding: FragmentProgresDetailBinding? = null
    private val binding get() = _binding!!

    // Menggunakan delegate by viewModels() standar Android KTX agar seirama dengan TugasFragment
    private val viewModel: TugasViewModel by viewModels()
    private lateinit var adapter: ProgresVillaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgresDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProgresVillaAdapter()

        binding.rvProgresDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProgresDetail.adapter = adapter

        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { groups ->
            adapter.setList(groups ?: emptyList())
        }

        // PERBAIKAN: Ambil UID manager aktif saat ini dan kirim sebagai parameter query root
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (managerUid.isNotEmpty()) {
            viewModel.getTugasGroupedByVilla(managerUid)
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}