package com.indri.vsmentproject.ui.common.profile

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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.FragmentProfileBinding
import com.indri.vsmentproject.databinding.DialogEditProfileBinding

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
        viewModel.getData()

        // 1. Sinkronisasi Data User
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            with(binding) {
                tvNamaUser.text = user.nama
                tvJabatanUser.text = "${user.posisi} (${user.role.uppercase()})"
                ivProfile.load(user.foto_profil) {
                    crossfade(true)
                    placeholder(R.drawable.ic_profile)
                    transformations(CircleCropTransformation())
                }

                // Ubah Label Dinamis
                if (user.role == "staff") {
                    tvLabelVilla.text = "Selesai"
                    tvLabelStaff.text = "Proses"
                    tvLabelLaporan.text = "Total"
                } else {
                    tvLabelVilla.text = "Villa"
                    tvLabelStaff.text = "Staff"
                    tvLabelLaporan.text = "Pending"
                }
            }
        }

        // 2. Sinkronisasi Statistik
        viewModel.summary.observe(viewLifecycleOwner) { stat ->
            with(binding) {
                tvCountVilla.text = stat.totalVilla.toString()
                tvCountStaff.text = stat.totalStaff.toString()
                tvCountLaporan.text = stat.totalLaporanPending.toString()
            }
        }

        // 3. Click Listeners
        binding.btnEditFoto.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnEditProfile.setOnClickListener { showEditDialog() }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            activity?.finish()
        }
    }

    private fun showEditDialog() {
        val user = viewModel.userData.value ?: return
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

        dialogBinding.etEditNama.setText(user.nama)
        dialogBinding.etEditTelp.setText(user.telepon)
        dialogBinding.etEditEmail.setText(user.email)

        AlertDialog.Builder(requireContext())
            .setTitle("Perbarui Profil")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                viewModel.updateFullProfile(
                    dialogBinding.etEditNama.text.toString(),
                    dialogBinding.etEditTelp.text.toString(),
                    dialogBinding.etEditEmail.text.toString()
                ) { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            }.setNegativeButton("Batal", null).show()
    }

    private fun uploadImage(uri: Uri) {
        Toast.makeText(context, "Mengunggah...", Toast.LENGTH_SHORT).show()
        MediaManager.get().upload(uri).callback(object : UploadCallback {
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val url = resultData?.get("secure_url").toString()
                val user = viewModel.userData.value!!
                viewModel.updateFullProfile(user.nama, user.telepon, user.email, url) {
                    Toast.makeText(context, "Foto berhasil diubah!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onError(requestId: String?, error: ErrorInfo?) {
                Toast.makeText(context, "Gagal: ${error?.description}", Toast.LENGTH_SHORT).show()
            }
            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        }).dispatch()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}