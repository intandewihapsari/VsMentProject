package com.indri.vsmentproject.ui.manager.masterdata

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentTambahVillaBinding

class TambahVillaFragment : Fragment() {

    private var _binding: FragmentTambahVillaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var villaEdit: VillaModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahVillaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // CEK MODE EDIT
        // =========================
        villaEdit = arguments?.getParcelable(ARG_VILLA)

        villaEdit?.let {
            binding.etNamaVilla.setText(it.nama)
            binding.etAlamat.setText(it.alamat)
            binding.etRuangan.setText(it.area.joinToString(", "))
            binding.etFasilitas.setText(it.fasilitas.joinToString(", "))

            // optional kalau mau tampil foto lama:
            Glide.with(this).load(it.foto_villa).into(binding.ivFotoVilla)
        }

        // =========================
        // PICK IMAGE
        // =========================
        binding.ivFotoVilla.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // =========================
        // SIMPAN
        // =========================
        binding.btnSimpan.setOnClickListener {
            simpanDataVilla()
        }
    }

    // IMAGE PICKER
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivFotoVilla.setImageURI(uri)
            }
        }

    // =========================
    // SAVE / UPDATE
    // =========================
    private fun simpanDataVilla() {

        val nama = binding.etNamaVilla.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val ruanganRaw = binding.etRuangan.text.toString().trim()
        val fasilitasRaw = binding.etFasilitas.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty() || ruanganRaw.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        val listRuangan = ruanganRaw.split(",")
            .map { it.trim().replaceFirstChar { c -> c.uppercase() } }
            .filter { it.isNotEmpty() }

        val listFasilitas = fasilitasRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val managerUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val idFinal = villaEdit?.id ?: "V${System.currentTimeMillis()}"

        // =========================
        // MODE EDIT TANPA GANTI FOTO
        // =========================
        if (villaEdit != null && selectedImageUri == null) {

            val data = mapOf(
                "id" to idFinal,
                "manager_id" to managerUid,
                "nama" to nama,
                "alamat" to alamat,
                "area" to listRuangan,
                "fasilitas" to listFasilitas,
                "foto_villa" to villaEdit!!.foto_villa,
                "status_tersedia" to true
            )

            viewModel.simpanVilla(idFinal, data)

            Toast.makeText(requireContext(), "Villa berhasil diupdate!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // =========================
        // UPLOAD IMAGE (CREATE / EDIT WITH NEW IMAGE)
        // =========================
        val imageUri = selectedImageUri

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Pilih gambar dulu", Toast.LENGTH_SHORT).show()
            return
        }

        CloudinaryHelper.uploadImage(imageUri, "villa") { result ->

            when (result) {

                is Resource.Loading -> {}

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Upload gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {

                    val response = result.data ?: return@uploadImage
                    val url = response.secure_url

                    val data = mapOf(
                        "id" to idFinal,
                        "manager_id" to managerUid,
                        "nama" to nama,
                        "alamat" to alamat,
                        "area" to listRuangan,
                        "fasilitas" to listFasilitas,
                        "foto_villa" to url,
                        "status_tersedia" to true
                    )

                    viewModel.simpanVilla(idFinal, data)

                    Toast.makeText(requireContext(), "Villa berhasil disimpan!", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    companion object {

        private const val ARG_VILLA = "arg_villa"

        fun newInstance(villa: VillaModel): TambahVillaFragment {
            return TambahVillaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_VILLA, villa)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}