package com.indri.vsmentproject.ui.manager.profile

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getData()

        // Observasi Statistik
        viewModel.summary.observe(viewLifecycleOwner) { stat ->
            binding.tvCountVilla.text = stat.totalVilla.toString()
            binding.tvCountStaff.text = stat.totalStaff.toString()
            binding.tvCountLaporan.text = stat.totalLaporanPending.toString()
        }

        // SINKRONISASI: Menggunakan UserModel dan field 'posisi'
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.tvNamaManager.text = user.nama
            binding.tvJabatan.text = user.posisi // Sesuai permintaan: posisi sebagai jabatan

            // Jika kamu ingin menampilkan nomor telepon
            // binding.tvTelepon.text = user.telepon
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logout Berhasil", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}