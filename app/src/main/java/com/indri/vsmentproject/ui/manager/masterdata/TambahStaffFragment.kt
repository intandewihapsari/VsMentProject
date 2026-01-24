package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentTambahStaffBinding

class TambahStaffFragment : Fragment() {

    private var _binding: FragmentTambahStaffBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSimpanStaff.setOnClickListener {
            registerStaffKeFirebase()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun registerStaffKeFirebase() {
        val nama = binding.etNamaStaff.text.toString().trim()
        val email = binding.etEmailStaff.text.toString().trim()
        val posisi = binding.etPosisiStaff.text.toString().trim()
        val password = binding.etPasswordStaff.text.toString().trim()

        if (nama.isEmpty() || email.isEmpty() || posisi.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Inisialisasi Secondary App agar Manager tidak ter-logout saat mendaftarkan Staff
        val options = FirebaseApp.getInstance().options
        val secondaryApp = try {
            FirebaseApp.initializeApp(requireContext(), options, "secondary")
        } catch (e: Exception) {
            FirebaseApp.getInstance("secondary")
        }

        FirebaseAuth.getInstance(secondaryApp).createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""

                // Menyusun data sesuai StaffModel
                val dataStaff = mapOf(
                    "uid" to uid,
                    "nama" to nama,
                    "email" to email,
                    "posisi" to posisi,
                    "role" to "staff",
                    "status" to "aktif"
                )

                // Simpan ke path users/staffs
                FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_STAFFS)
                    .child(uid)
                    .setValue(dataStaff)
                    .addOnCompleteListener {
                        FirebaseAuth.getInstance(secondaryApp).signOut() // Logout secondary app
                        Toast.makeText(requireContext(), "Staff $nama berhasil didaftarkan!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}