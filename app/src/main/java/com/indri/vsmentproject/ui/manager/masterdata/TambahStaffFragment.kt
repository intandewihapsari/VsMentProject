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
            Toast.makeText(requireContext(), "Isi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val options = FirebaseApp.getInstance().options
        val secondaryApp = FirebaseApp.initializeApp(requireContext(), options, "secondary")

        FirebaseAuth.getInstance(secondaryApp!!)
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val uid = it.user?.uid ?: ""
                val managerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                val data = mapOf(
                    "uid" to uid,
                    "nama" to nama,
                    "email" to email,
                    "posisi" to posisi,
                    "role" to "staff",
                    "manager_id" to managerId,
                    "foto_profil" to "",
                    "status" to "aktif"
                )

                FirebaseDatabase.getInstance()
                    .getReference(FirebaseConfig.PATH_STAFFS)
                    .child(uid)
                    .setValue(data)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Berhasil tambah staff", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
            }
    }    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}