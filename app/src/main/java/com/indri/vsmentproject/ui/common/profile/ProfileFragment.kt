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

        // 2. Sinkronisasi Statistik Angka
        viewModel.summary.observe(viewLifecycleOwner) { stat ->
            with(binding) {
                // Silakan sesuaikan ID komponen TextView (seperti tvStat1, dll)
                // dengan nama ID yang ada pada file fragment_profile.xml Anda.
//                if (viewModel.userData.value?.role == "manager") {
//                    tvStat1.text = "${stat.totalVilla} Villa"
//                    tvStat2.text = "${stat.totalStaff} Staff"
//                    tvStat3.text = "${stat.totalLaporanPending} Pending"
//                } else {
//                    // Mapping untuk role staff (menyesuaikan isi kalkulasi di ViewModel)
//                    tvStat1.text = "${stat.totalVilla} Selesai"      // totalBeres
//                    tvStat2.text = "${stat.totalStaff} Laporan"      // totalLaporan
//                    tvStat3.text = "${stat.totalLaporanPending} Sisa" // sisaTugas
//                }
            }
        }

        // 3. Click Listeners utama fragment
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

        // Tampilkan data profil saat ini ke dalam EditText
        dialogBinding.etEditNama.setText(user.nama)
        dialogBinding.etEditTelp.setText(user.telepon)
        dialogBinding.etEditEmail.setText(user.email)

        // Buat Dialog tanpa menggunakan Positive/Negative Button bawaan builder
        // agar tombol custom di XML (btnSave dan btnCancel) yang memegang kendali penuh
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Listener Aksi Tombol Batal Custom XML
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Listener Aksi Tombol Simpan Custom XML
        dialogBinding.btnSave.setOnClickListener {
            val newNama = dialogBinding.etEditNama.text.toString().trim()
            val newTelp = dialogBinding.etEditTelp.text.toString().trim()
            val newEmail = dialogBinding.etEditEmail.text.toString().trim()

            if (newNama.isNotEmpty() && newEmail.isNotEmpty()) {
                viewModel.updateFullProfile(newNama, newTelp, newEmail) { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    dialog.dismiss() // Dialog ditutup hanya jika berhasil menyimpan data
                }
            } else {
                Toast.makeText(context, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

        // Membuat latar belakang bawaan dialog transparan agar sudut melengkung CardView terlihat rapi
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun uploadImage(uri: Uri) {
        val targetFolder = if (viewModel.userData.value?.role == "manager") "manager" else "staff"

        CloudinaryHelper.uploadImage(uri, targetFolder) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(context, "Mengunggah foto...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    val secureUrl = resource.data?.secure_url ?: ""
                    val user = viewModel.userData.value ?: return@uploadImage

                    viewModel.updateFullProfile(user.nama, user.telepon, user.email, secureUrl) {
                        Toast.makeText(context, "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
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