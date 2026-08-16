package com.indri.vsmentproject.ui.staff.report

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.FragmentLaporanStaffBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LaporanStaffFragment : Fragment() {

    private var _binding: FragmentLaporanStaffBinding? = null
    private val binding get() = _binding!!
    private var currentPhotoUri: Uri? = null
    private val villaNames = mutableListOf<String>()
    private val villaIds = mutableListOf<String>()
    private var managerId: String? = null
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCameraFlow() else Toast.makeText(context, "Izin Kamera Ditolak", Toast.LENGTH_SHORT).show()
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) currentPhotoUri?.let { showForm(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLaporanStaffBinding.inflate(inflater, container, false)
        CloudinaryHelper.init(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchManagerId()
        setupStatusDropdown()
        checkIncomingArguments()

        binding.ivPreviewForm.setOnClickListener {
            openGallery()
        }
        binding.btnLaporkan.setOnClickListener { validateAndUpload() }

        openGallery()
    }

    private fun checkIncomingArguments() {
        arguments?.let {
            binding.actvVilla.setText(it.getString("VILLA_NAMA"), false)
            binding.actvLokasi.setText(it.getString("RUANGAN_NAMA"), false)
            binding.actvLokasi.isEnabled = true
            binding.etNamaBarang.setText(it.getString("BARANG_NAMA"))
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraFlow()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCameraFlow() {
        binding.layoutForm.visibility = View.GONE
        binding.layoutCamera.visibility = View.VISIBLE
        val photoFile = File.createTempFile("IMG_LAPOR_", ".jpg", requireContext().cacheDir)
        currentPhotoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        cameraLauncher.launch(currentPhotoUri)
    }

//    private fun showForm(uri: Uri) {
//        binding.layoutCamera.visibility = View.GONE
//        binding.layoutForm.visibility = View.VISIBLE
//        binding.ivPreviewForm.setImageURI(uri)
//    }
    private fun showForm(uri: Uri) {
        currentPhotoUri = uri
        binding.layoutCamera.visibility = View.GONE
        binding.layoutForm.visibility = View.VISIBLE
        binding.ivPreviewForm.setImageURI(uri)
    }
    private fun openGallery() {
        binding.layoutForm.visibility = View.GONE
        binding.layoutCamera.visibility = View.VISIBLE
        galleryLauncher.launch("image/*")
    }

    private fun fetchManagerId() {
        val uid = auth.currentUser?.uid ?: return
        db.child(FirebaseConfig.PATH_USER_MAPPING).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    managerId = snapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()
                    if (managerId != "null" && !managerId.isNullOrEmpty()) {
                        setupVillaDropdown()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupVillaDropdown() {
        val mId = managerId ?: return
        FirebaseConfig.getVillasRef(mId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                villaNames.clear()
                villaIds.clear()
                for (ds in snapshot.children) {
                    villaNames.add(ds.child("nama").value.toString())
                    villaIds.add(ds.key ?: "")
                }
                if (isAdded && _binding != null) {
                    binding.actvVilla.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, villaNames))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.actvVilla.setOnItemClickListener { _, _, position, _ ->
            fetchAreas(villaIds[position])
        }
    }

    private fun fetchAreas(villaId: String) {
        val mId = managerId ?: return
        binding.actvLokasi.setText("")
        FirebaseConfig.getVillasRef(mId).child(villaId).child("area")
            .get().addOnSuccessListener { snapshot ->
                if (isAdded && _binding != null) {
                    val areas = snapshot.children.map { it.value.toString() }
                    binding.actvLokasi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, areas))
                    binding.actvLokasi.isEnabled = true
                }
            }
    }

    private fun setupStatusDropdown() {
        binding.actvKondisi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, arrayOf("Habis", "Rusak", "Hilang")))
        binding.actvKondisi.setText("Rusak", false)
    }

    private fun validateAndUpload() {
        val photoUri = currentPhotoUri
        if (binding.etNamaBarang.text.isEmpty() || photoUri == null) {
            Toast.makeText(context, "Mohon lengkapi data dan pilih foto!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLaporkan.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        CloudinaryHelper.uploadImage(photoUri, "laporan") { res ->
            if (_binding == null) return@uploadImage
            
            if (res is Resource.Success) {
                saveToFirebase(res.data?.secure_url ?: "")
            } else if (res is Resource.Error) {
                binding.btnLaporkan.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, res.message ?: "Gagal upload gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun generateReportId(callback: (String) -> Unit) {
        val mId = managerId ?: return
        FirebaseConfig.getLaporanKerusakanRef(mId).get().addOnSuccessListener { snapshot ->
            val count = snapshot.childrenCount.toInt() + 1
            val newId = "REP_" + String.format("%03d", count)
            callback(newId)
        }
    }

    private fun saveToFirebase(url: String) {
        val mId = managerId ?: return
        val dbLaporan = FirebaseConfig.getLaporanKerusakanRef(mId)

        val pref = requireActivity()
            .getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val newRef = db.push()
        val firebaseKey = newRef.key ?: return

        val selectedVillaIndex =
            villaNames.indexOf(binding.actvVilla.text.toString())

        val selectedVillaId =
            if (selectedVillaIndex != -1)
                villaIds[selectedVillaIndex]
            else ""

        generateReportId { newId ->

            val laporan = LaporanModel(
                id = newId,
                villa_id = selectedVillaId,
                villa_nama = binding.actvVilla.text.toString(),
                area = binding.actvLokasi.text.toString(),
                staff_id = auth.currentUser?.uid ?: "",
                staff_nama = pref.getString("nama", "Staff") ?: "",
                tipe_laporan = binding.actvKondisi.text.toString(),
                nama_barang = binding.etNamaBarang.text.toString(),
                deskripsi = binding.etDeskripsi.text.toString(),
                foto_url = url,
                status = "pending",
                catatan_manager = "",
                created_at = System.currentTimeMillis(),
                waktu_lapor = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                ).format(Date()),
                waktu_selesai = ""
            )

            dbLaporan.push().setValue(laporan).addOnCompleteListener {
                binding.btnLaporkan.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (it.isSuccessful) {
                    Toast.makeText(context, "Laporan Berhasil!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Gagal kirim laporan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { showForm(it) }
    }

    // private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//     if (isGranted) startCameraFlow() else Toast.makeText(context, "Izin Kamera Ditolak", Toast.LENGTH_SHORT).show()
// }

    // private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//     if (success) currentPhotoUri?.let { showForm(it) }
// }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}