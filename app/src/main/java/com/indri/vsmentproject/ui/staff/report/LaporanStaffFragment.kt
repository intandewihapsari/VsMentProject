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
import com.indri.vsmentproject.data.utils.NetworkUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LaporanStaffFragment : Fragment() {

    private var _binding: FragmentLaporanStaffBinding? = null
    private val binding get() = _binding!!
    private val capturedPhotos = mutableListOf<Uri>()
    private lateinit var fotoAdapter: com.indri.vsmentproject.ui.staff.common.CapturedFotoAdapter

    private val villaNames = mutableListOf<String>()
    private val villaIds = mutableListOf<String>()
    private var managerId: String? = null
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

    private var currentPhotoUri: Uri? = null
    private var isCameraLaunching = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCameraFlow()
        } else {
            Toast.makeText(context, "Izin Kamera diperlukan untuk mengambil bukti foto", Toast.LENGTH_LONG).show()
            showForm()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        isCameraLaunching = false
        if (success) {
            currentPhotoUri?.let { uri ->
                capturedPhotos.add(uri)
                fotoAdapter.updateData(capturedPhotos)
            }
        }
        showForm() 
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLaporanStaffBinding.inflate(inflater, container, false)
        CloudinaryHelper.init(requireContext())
        
        savedInstanceState?.let { bundle ->
            currentPhotoUri = bundle.getParcelable("current_uri")
            val restoredList = bundle.getParcelableArrayList<Uri>("captured_list")
            if (restoredList != null) {
                capturedPhotos.clear()
                capturedPhotos.addAll(restoredList)
            }
        }
        
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("current_uri", currentPhotoUri)
        outState.putParcelableArrayList("captured_list", ArrayList(capturedPhotos))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchManagerId()
        setupStatusDropdown()
        checkIncomingArguments()

        if (capturedPhotos.isEmpty() && !isCameraLaunching && savedInstanceState == null) {
            binding.root.post {
                if (isAdded && _binding != null && capturedPhotos.isEmpty()) {
                    startCameraFlow()
                }
            }
        }

        binding.btnAmbilFoto.setOnClickListener {
            if (capturedPhotos.size >= 5) {
                Toast.makeText(context, "Maksimal 5 foto!", Toast.LENGTH_SHORT).show()
            } else {
                startCameraFlow()
            }
        }
        binding.btnLaporkan.setOnClickListener { validateAndUpload() }
    }

    private fun checkIncomingArguments() {
        arguments?.let {
            binding.actvVilla.setText(it.getString("VILLA_NAMA"), false)
            binding.actvLokasi.setText(it.getString("RUANGAN_NAMA"), false)
            binding.actvLokasi.isEnabled = true
            binding.etNamaBarang.setText(it.getString("BARANG_NAMA"))
        }
    }

    private fun setupRecyclerView() {
        fotoAdapter = com.indri.vsmentproject.ui.staff.common.CapturedFotoAdapter { position ->
            capturedPhotos.removeAt(position)
            fotoAdapter.updateData(capturedPhotos)
        }
        binding.rvCapturedPhotos.adapter = fotoAdapter
    }

    private fun startCameraFlow() {
        if (isCameraLaunching) return

        // Cek Izin Kamera di Runtime
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        
        try {
            isCameraLaunching = true
            binding.layoutForm.visibility = View.GONE
            binding.layoutCamera.visibility = View.VISIBLE
            
            val storageDir = requireContext().externalCacheDir ?: requireContext().cacheDir
            val photoFile = File.createTempFile(
                "IMG_LAPOR_${System.currentTimeMillis()}_", 
                ".jpg", 
                storageDir
            )
            
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(), 
                "com.indri.vsmentproject.fileprovider", 
                photoFile
            )
            
            currentPhotoUri?.let {
                cameraLauncher.launch(it)
            } ?: run {
                throw Exception("Gagal membuat URI Foto")
            }

        } catch (e: Exception) {
            isCameraLaunching = false
            Toast.makeText(context, "Gagal membuka kamera: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            showForm()
        }
    }

    private fun showForm() {
        if (_binding == null) return
        binding.layoutCamera.visibility = View.GONE
        binding.layoutForm.visibility = View.VISIBLE
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
        if (binding.etNamaBarang.text.isEmpty() || capturedPhotos.size < 3) {
            Toast.makeText(context, "Ups! Nama barang atau jumlah foto (minimal 3) belum lengkap nih.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(context, "Sinyal lagi 'sembunyi' nih. Pastikan internet kamu aktif untuk kirim foto ya!", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnLaporkan.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        Toast.makeText(context, "Sedang mengirim laporan... Mohon tunggu sebentar ya!", Toast.LENGTH_SHORT).show()

        uploadAllPhotos { listUrl ->
            saveToFirebase(listUrl)
        }
    }

    private fun uploadAllPhotos(onComplete: (List<String>) -> Unit) {
        val urls = mutableListOf<String>()
        var count = 0
        
        capturedPhotos.forEach { uri ->
            CloudinaryHelper.uploadImage(uri, "laporan") { res ->
                if (_binding == null) return@uploadImage
                if (res is Resource.Success) {
                    res.data?.secure_url?.let { urls.add(it) }
                    count++
                    if (count == capturedPhotos.size) onComplete(urls)
                } else if (res is Resource.Error) {
                    binding.btnLaporkan.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Waduh, kirim fotonya gagal: ${res.message}. Coba cek sinyal atau kirim ulang ya!", Toast.LENGTH_LONG).show()
                }
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

    private fun saveToFirebase(urls: List<String>) {
        val mId = managerId ?: return
        val dbLaporan = FirebaseConfig.getLaporanKerusakanRef(mId)

        val pref = requireActivity()
            .getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val newRef = dbLaporan.push()

        val selectedVillaIndex = villaNames.indexOf(binding.actvVilla.text.toString())
        val selectedVillaId = if (selectedVillaIndex != -1) villaIds[selectedVillaIndex] else ""

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
                foto_url = urls.firstOrNull() ?: "",
                bukti_foto = urls,
                status = "pending",
                catatan_manager = "",
                created_at = System.currentTimeMillis(),
                waktu_lapor = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                waktu_selesai = ""
            )

            // Persiapan Batch Update (Laporan + Link Task)
            val updates = HashMap<String, Any>()
            val reportPath = newRef.key ?: return@generateReportId
            updates["operational/laporan_kerusakan/$reportPath"] = laporan

            // Jika laporan ini dipicu dari sebuah Tugas, tandai Tugas tersebut Selesai
            val linkedTaskId = arguments?.getString("TASK_ID")
            val linkedVillaId = arguments?.getString("VILLA_ID")
            
            if (!linkedTaskId.isNullOrEmpty() && !linkedVillaId.isNullOrEmpty()) {
                val taskPath = "operational/task_management/$linkedVillaId/list_tugas/$linkedTaskId"
                updates["$taskPath/status"] = FirebaseConfig.STATUS_DONE
                updates["$taskPath/completed_at"] = System.currentTimeMillis()
                updates["$taskPath/bukti_foto"] = urls // Gunakan foto laporan sebagai bukti tugas
                updates["$taskPath/is_validated"] = true
            }

            FirebaseConfig.getManagerRef(mId).updateChildren(updates).addOnCompleteListener {
                binding.btnLaporkan.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (it.isSuccessful) {
                    val msg = if (!linkedTaskId.isNullOrEmpty()) "Laporan Berhasil & Tugas Selesai!" else "Laporan Berhasil!"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Gagal kirim laporan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
