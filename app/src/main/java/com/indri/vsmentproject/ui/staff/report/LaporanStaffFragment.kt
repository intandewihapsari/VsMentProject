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

        setupVillaDropdown()
        setupStatusDropdown()
        checkIncomingArguments()

        binding.ivPreviewForm.setOnClickListener { checkCameraPermission() }
        binding.btnLaporkan.setOnClickListener { validateAndUpload() }

        checkCameraPermission()
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

    private fun showForm(uri: Uri) {
        binding.layoutCamera.visibility = View.GONE
        binding.layoutForm.visibility = View.VISIBLE
        binding.ivPreviewForm.setImageURI(uri)
    }

    private fun setupVillaDropdown() {
        val dbVillas = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_VILLAS)
        dbVillas.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                villaNames.clear()
                villaIds.clear()
                for (ds in snapshot.children) {
                    villaNames.add(ds.child("nama").value.toString())
                    villaIds.add(ds.key ?: "")
                }
                binding.actvVilla.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, villaNames))
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.actvVilla.setOnItemClickListener { _, _, position, _ ->
            fetchAreas(villaIds[position])
        }
    }

    private fun fetchAreas(villaId: String) {
        binding.actvLokasi.setText("")
        FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_VILLAS).child(villaId).child("area")
            .get().addOnSuccessListener { snapshot ->
                val areas = snapshot.children.map { it.value.toString() }
                binding.actvLokasi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, areas))
                binding.actvLokasi.isEnabled = true
            }
    }

    private fun setupStatusDropdown() {
        binding.actvKondisi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, arrayOf("Habis", "Rusak", "Hilang")))
        binding.actvKondisi.setText("Rusak", false)
    }

    private fun validateAndUpload() {
        if (binding.etNamaBarang.text.isEmpty() || currentPhotoUri == null) {
            Toast.makeText(context, "Mohon lengkapi data!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLaporkan.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        CloudinaryHelper.uploadImage(currentPhotoUri!!, "laporan") { res ->
            if (res is Resource.Success) {
                saveToFirebase(res.data?.secure_url ?: "")
            } else if (res is Resource.Error) {
                binding.btnLaporkan.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun generateReportId(callback: (String) -> Unit) {
        val db = FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

        db.get().addOnSuccessListener { snapshot ->
            val count = snapshot.childrenCount.toInt() + 1
            val newId = "REP_" + String.format("%03d", count)
            callback(newId)
        }
    }

    private fun saveToFirebase(url: String) {
        val db = FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

        val pref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val newRef = db.push() // ✅ ini key asli firebase
        val firebaseKey = newRef.key ?: return

        // 🔥 ambil index villa
        val selectedVillaIndex = villaNames.indexOf(binding.actvVilla.text.toString())
        val selectedVillaId = if (selectedVillaIndex != -1) villaIds[selectedVillaIndex] else ""

        val laporan = LaporanModel(
            id = firebaseKey, // tetap pakai ini biar aman

            villa_id = selectedVillaId,
            villa_nama = binding.actvVilla.text.toString(),
            area = binding.actvLokasi.text.toString(),

            staff_id = pref.getString("staff_id", "") ?: "",
            staff_nama = pref.getString("nama", "Staff") ?: "",

            tipe_laporan = binding.actvKondisi.text.toString(),
            nama_barang = binding.etNamaBarang.text.toString(),
            deskripsi = binding.etDeskripsi.text.toString(),

            foto_url = url,
            status = "pending",
            catatan_manager = "",

            waktu_lapor = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            waktu_selesai = ""
        )

        newRef.setValue(laporan).addOnCompleteListener {
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
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}