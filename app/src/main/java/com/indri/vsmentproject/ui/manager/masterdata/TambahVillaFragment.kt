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
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentTambahVillaBinding
import com.indri.vsmentproject.ui.main.ManagerActivity

class TambahVillaFragment : Fragment() {

    private var _binding: FragmentTambahVillaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var villaEdit: VillaModel? = null
    private var isEditMode = false

    // =============================
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahVillaBinding.inflate(inflater, container, false)
        return binding.root
    }

    // =============================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val villa = arguments?.getParcelable<VillaModel>(ARG_VILLA)

        if (villa != null) {
            setupEditMode(villa)
        } else {
            setupAddMode()
        }

        // PICK IMAGE
        binding.ivFotoVilla.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // BACK
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // SIMPAN
        binding.btnSimpan.setOnClickListener {
            simpanDataVilla()
        }
    }

    // =============================
    private fun setupAddMode() {
        isEditMode = false
        binding.tvTitle.text = "Tambah Villa"
        binding.btnSimpan.text = "Simpan Data"
    }

    // =============================
    private fun setupEditMode(villa: VillaModel) {
        isEditMode = true
        villaEdit = villa

        binding.tvTitle.text = "Edit Villa"
        binding.btnSimpan.text = "Update Data"

        binding.etNamaVilla.setText(villa.nama)
        binding.etAlamat.setText(villa.alamat)
        binding.etRuangan.setText(villa.area.joinToString(", "))
        binding.etFasilitas.setText(villa.fasilitas.joinToString(", "))

        if (villa.foto_villa.isNotEmpty()) {
            Glide.with(this)
                .load(villa.foto_villa)
                .into(binding.ivFotoVilla)
        }
    }

    // =============================
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivFotoVilla.setImageURI(uri)
            }
        }

    // =============================
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
        val imageUri = selectedImageUri

        // =============================
        // EDIT TANPA GANTI FOTO
        // =============================
        if (isEditMode && imageUri == null) {

            val villa = VillaModel(
                id = idFinal,
                manager_id = managerUid,
                nama = nama,
                alamat = alamat,
                deskripsi = "",
                area = listRuangan,
                fasilitas = listFasilitas,
                foto_villa = villaEdit?.foto_villa ?: "",
                status_tersedia = true
            )

            viewModel.simpanVilla(idFinal, villa)

            Toast.makeText(requireContext(), "Villa berhasil diupdate!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // =============================
        // VALIDASI FOTO SAAT CREATE
        // =============================
        if (!isEditMode && imageUri == null) {
            Toast.makeText(requireContext(), "Pilih gambar dulu", Toast.LENGTH_SHORT).show()
            return
        }

        // =============================
        // UPLOAD FOTO (CREATE / EDIT)
        // =============================
        if (imageUri != null) {

            Toast.makeText(requireContext(), "Uploading foto...", Toast.LENGTH_SHORT).show()

            CloudinaryHelper.uploadImage(imageUri, "villa") { result ->

                when (result) {

                    is Resource.Success -> {

                        val url = result.data?.secure_url

                        if (url.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Upload gagal (URL kosong)", Toast.LENGTH_SHORT).show()
                            return@uploadImage
                        }

                        val villa = VillaModel(
                            id = idFinal,
                            manager_id = managerUid,
                            nama = nama,
                            alamat = alamat,
                            deskripsi = "",
                            area = listRuangan,
                            fasilitas = listFasilitas,
                            foto_villa = url,
                            status_tersedia = true
                        )

                        viewModel.simpanVilla(idFinal, villa)

                        Toast.makeText(requireContext(), "Villa berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), result.message ?: "Upload gagal", Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }
    }

    // =============================
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

    // =============================
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}