package com.indri.vsmentproject.ui.manager.task.progressVilla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentProgresDetailBinding
import com.indri.vsmentproject.ui.manager.task.TugasViewModel

class ProgresDetailFragment : Fragment() {

    private var _binding: FragmentProgresDetailBinding? = null
    private val binding get() = _binding!!

    // Hubungkan ke ViewModel yang sama
    private val viewModel: TugasViewModel by viewModels()
    private lateinit var progresAdapter: ProgresVillaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgresDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Adapter khusus Progres
        progresAdapter = ProgresVillaAdapter()

        binding.rvProgresDetail.apply {
            adapter = progresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 2. Amati data rekap villa (bukan detail tugas)
        viewModel.rawGroupsLive.observe(viewLifecycleOwner) { dataRekap ->
            if (!dataRekap.isNullOrEmpty()) {
                // Sekarang 'data' tipenya sudah List<VillaTugasGroup>
                progresAdapter.setList(dataRekap)
            }
        }

        // 3. Panggil data dari Firebase
        viewModel.getTugasGroupedByVilla()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}