package com.indri.vsmentproject.ui.manager.masterdata

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentTambahStaffBinding
import com.indri.vsmentproject.ui.main.ManagerActivity

class TambahStaffFragment : Fragment() {

    private var _binding: FragmentTambahStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    private var isEditMode = false
    private var staffId: String? = null

    // =============================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    // =============================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val staff = arguments?.getParcelable<UserModel>(ARG_STAFF)

        if (staff != null) {
            setupEditMode(staff)
        } else {
            setupAddMode()
        }

        binding.ivFotoStaff.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSimpanStaff.setOnClickListener {
            if (isEditMode) {
                updateStaff()
            } else {
                createStaff()
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // =============================
    private fun setupAddMode() {
        isEditMode = false
        binding.tvTitle.text = "Tambah Staff"
        binding.btnSimpanStaff.text = "Simpan Data"
        binding.etPasswordStaff.visibility = View.VISIBLE
    }

    // =============================
    private fun setupEditMode(staff: UserModel) {
        isEditMode = true
        staffId = staff.uid

        binding.tvTitle.text = "Edit Staff"
        binding.btnSimpanStaff.text = "Update Data"

        binding.etNamaStaff.setText(staff.nama)
        binding.etEmailStaff.setText(staff.email)
        binding.etPosisiStaff.setText(staff.posisi)
        binding.etTeleponStaff.setText(staff.telepon)

        binding.swStatusStaff.isChecked = staff.status == "aktif"

        binding.etPasswordStaff.visibility = View.GONE

        if (staff.foto_profil.isNotEmpty()) {
            Glide.with(this)
                .load(staff.foto_profil)
                .into(binding.ivFotoStaff)
        }
    }

    // =============================
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivFotoStaff.setImageURI(uri)
            }
        }

    // =============================
    // 🔥 CREATE STAFF
    // =============================
    private fun createStaff() {

        val nama = binding.etNamaStaff.text.toString().trim()
        val email = binding.etEmailStaff.text.toString().trim()
        val posisi = binding.etPosisiStaff.text.toString().trim()
        val password = binding.etPasswordStaff.text.toString().trim()
        val telepon = binding.etTeleponStaff.text.toString().trim()
        val status = if (binding.swStatusStaff.isChecked) "aktif" else "nonaktif"

        if (nama.isEmpty() || email.isEmpty() || posisi.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val managerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { auth ->

                val uid = auth.user?.uid ?: ""

                val imageUri = selectedImageUri

                if (imageUri != null) {

                    Toast.makeText(requireContext(), "Uploading foto...", Toast.LENGTH_SHORT).show()

                    CloudinaryHelper.uploadImage(imageUri, "staff") { result ->

                        when (result) {

                            is Resource.Success -> {
                                val url = result.data?.secure_url

                                if (url.isNullOrEmpty()) {
                                    Toast.makeText(requireContext(), "Upload gagal (URL kosong)", Toast.LENGTH_SHORT).show()
                                    return@uploadImage
                                }

                                simpanKeFirebase(uid, nama, email, posisi, telepon, status, url, managerId)
                            }

                            is Resource.Error -> {
                                Toast.makeText(requireContext(), "Upload gagal: ${result.message}", Toast.LENGTH_LONG).show()
                            }

                            else -> {}
                        }
                    }

                } else {
                    simpanKeFirebase(uid, nama, email, posisi, telepon, status, "", managerId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal membuat akun: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // =============================
    // 🔥 UPDATE STAFF (FIX TOTAL)
    // =============================
    private fun updateStaff() {

        val nama = binding.etNamaStaff.text.toString().trim()
        val email = binding.etEmailStaff.text.toString().trim()
        val posisi = binding.etPosisiStaff.text.toString().trim()
        val telepon = binding.etTeleponStaff.text.toString().trim()
        val status = if (binding.swStatusStaff.isChecked) "aktif" else "nonaktif"

        val id = staffId ?: return

        val imageUri = selectedImageUri

        if (imageUri != null) {

            Toast.makeText(requireContext(), "Uploading foto...", Toast.LENGTH_SHORT).show()

            CloudinaryHelper.uploadImage(imageUri, "staff") { result ->

                when (result) {

                    is Resource.Success -> {

                        val url = result.data?.secure_url

                        if (url.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "URL kosong!", Toast.LENGTH_SHORT).show()
                            return@uploadImage
                        }

                        val data = mapOf(
                            "nama" to nama,
                            "email" to email,
                            "posisi" to posisi,
                            "telepon" to telepon,
                            "status" to status,
                            "foto_profil" to url
                        )

                        viewModel.simpanStaff(id, data)

                        Toast.makeText(requireContext(), "Foto berhasil diupdate", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "Upload gagal: ${result.message}", Toast.LENGTH_LONG).show()
                    }

                    is Resource.Loading -> {}
                }
            }

        } else {

            val data = mapOf(
                "nama" to nama,
                "email" to email,
                "posisi" to posisi,
                "telepon" to telepon,
                "status" to status
            )

            viewModel.simpanStaff(id, data)

            Toast.makeText(requireContext(), "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    // =============================
    private fun simpanKeFirebase(
        uid: String,
        nama: String,
        email: String,
        posisi: String,
        telepon: String,
        status: String,
        fotoUrl: String,
        managerId: String
    ) {

        val data = mapOf(
            "uid" to uid,
            "nama" to nama,
            "email" to email,
            "posisi" to posisi,
            "telepon" to telepon,
            "status" to status,
            "foto_profil" to fotoUrl,
            "role" to "staff",
            "manager_id" to managerId
        )

        FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_STAFFS)
            .child(uid)
            .setValue(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Staff berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal simpan: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        private const val ARG_STAFF = "arg_staff"

        fun newInstance(staff: UserModel): TambahStaffFragment {
            return TambahStaffFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_STAFF, staff)
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}