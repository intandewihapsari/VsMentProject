package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.FragmentTambahVillaBinding

class TambahVillaFragment : Fragment() {

    private var _binding: FragmentTambahVillaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahVillaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSimpan.setOnClickListener {
            simpanDataVilla()
        }

        // Tombol back opsional jika kamu pakai header custom
        // binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun simpanDataVilla() {
        val nama = binding.etNamaVilla.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val ruanganRaw = binding.etRuangan.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty() || ruanganRaw.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        // LOGIKA ARRAY: Mengubah String "Kamar, Dapur" menjadi List ["Kamar", "Dapur"]
        val listRuangan = ruanganRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val idFix = "V${System.currentTimeMillis()}"
        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val data = mapOf(
            "id" to idFix,
            "manager_id" to managerUid,
            "nama" to nama,
            "alamat" to alamat,
            "areas" to listRuangan, // Tersimpan sebagai array di Firebase
            "foto" to "https://res.cloudinary.com/do8dnkpew/image/upload/v1766219984/default_villa.jpg",
            "status_tersedia" to true
        )

        viewModel.simpanVilla(idFix, data)
        Toast.makeText(requireContext(), "Villa $nama berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

        // Kembali ke halaman sebelumnya setelah sukses
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(villa: VillaModel) = TambahVillaFragment()
    }
}