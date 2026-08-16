package com.indri.vsmentproject.ui.common.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentProfileBinding
import com.indri.vsmentproject.databinding.DialogEditProfileBinding
import com.indri.vsmentproject.ui.auth.LoginActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadImage(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi konfigurasi Cloudinary sebelum proses upload dipakai
        CloudinaryHelper.init(requireContext())
        viewModel.getData()

        // 1. Sinkronisasi Data User & Label (AKTIF DAN SINKRON)
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            with(binding) {
                tvNamaUser.text = user.nama
                tvJabatanUser.text = "${user.posisi} (${user.role.uppercase()})"
                ivProfile.load(user.foto_profil) {
                    crossfade(true)
                    placeholder(R.drawable.ic_profile)
                    error(R.drawable.ic_profile)
                    transformations(CircleCropTransformation())
                }
            }
        }

        // 2. Click Listeners utama fragment
        binding.btnEditFoto.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnEditProfile.setOnClickListener { showEditDialog() }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    // Hapus session local sebelum logout
                    val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    sharedPref.edit().clear().apply()

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null).show()
        }
    }

    private fun showEditDialog() {
        val user = viewModel.userData.value ?: return
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

        dialogBinding.etEditNama.setText(user.nama)
        dialogBinding.etEditTelp.setText(user.telepon)
        dialogBinding.etEditEmail.setText(user.email)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false) // Prevent accidental dismissal during save
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val newNama = dialogBinding.etEditNama.text.toString().trim()
            val newTelp = dialogBinding.etEditTelp.text.toString().trim()
            val newEmail = dialogBinding.etEditEmail.text.toString().trim()

            // 1. Validasi Input
            if (newNama.isEmpty()) {
                dialogBinding.etEditNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                dialogBinding.etEditEmail.error = "Email tidak valid"
                return@setOnClickListener
            }

            if (newTelp.isNotEmpty() && newTelp.length < 10) {
                dialogBinding.etEditTelp.error = "Nomor telepon minimal 10 digit"
                return@setOnClickListener
            }

            // 2. Jalankan Update via ViewModel
            viewModel.updateFullProfile(newNama, newTelp, newEmail)
        }

        // 3. Observasi Status Update di dalam Dialog
        viewModel.updateStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    dialogBinding.progressBar.visibility = View.VISIBLE
                    dialogBinding.btnSave.isEnabled = false
                    dialogBinding.btnCancel.isEnabled = false
                }
                is Resource.Success -> {
                    dialogBinding.progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    // Reset status agar tidak terpicu ulang saat dialog dibuka lagi
                    // (ViewModel lifecycle lasts longer than dialog)
                }
                is Resource.Error -> {
                    dialogBinding.progressBar.visibility = View.GONE
                    dialogBinding.btnSave.isEnabled = true
                    dialogBinding.btnCancel.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun uploadImage(uri: Uri) {
        val targetFolder = if (viewModel.userData.value?.role == "manager") "manager" else "staff"

        CloudinaryHelper.uploadImage(uri, targetFolder) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    Toast.makeText(context, "Mengunggah foto...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val secureUrl = resource.data?.secure_url ?: ""
                    val user = viewModel.userData.value ?: return@uploadImage

                    viewModel.updateFullProfile(user.nama, user.telepon, user.email, secureUrl)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Upload gagal: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}