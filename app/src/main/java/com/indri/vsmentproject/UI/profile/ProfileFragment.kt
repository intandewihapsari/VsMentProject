package com.indri.vsmentproject.UI.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.indri.vsmentproject.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data statistik Manager
        viewModel.getData()

        // Observasi data dari ViewModel
        viewModel.summary.observe(viewLifecycleOwner) { stat ->
            binding.tvCountVilla.text = stat.totalVilla.toString()
            binding.tvCountStaff.text = stat.totalStaff.toString()
            binding.tvCountLaporan.text = stat.totalLaporanPending.toString()
        }

        // Contoh Nama & Jabatan (Hardcoded untuk Manager)
        binding.tvNamaManager.text = "Manager Admin"
        binding.tvJabatan.text = "General Manager Operasional"

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logout Berhasil", Toast.LENGTH_SHORT).show()
            // Tambahkan logic pindah ke LoginActivity di sini jika perlu
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}