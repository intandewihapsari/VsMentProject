package com.indri.vsmentproject.ui.staff.task

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.data.utils.NetworkUtils
import com.indri.vsmentproject.databinding.FragmentUploadBuktiBinding
import java.io.File
import java.util.HashMap

class UploadBuktiTugasFragment : Fragment() {

    private var _binding: FragmentUploadBuktiBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var tugas: TugasModel
    private lateinit var managerId: String

    private val capturedPhotos = mutableListOf<Uri>()
    private lateinit var fotoAdapter: com.indri.vsmentproject.ui.staff.common.CapturedFotoAdapter
    private var currentPhotoUri: Uri? = null
    private var isCameraLaunching = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCameraFlow()
        } else {
            Toast.makeText(context, "Izin Kamera diperlukan untuk mengambil bukti tugas", Toast.LENGTH_LONG).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        isCameraLaunching = false
        if (_binding != null) {
            binding.progressBar.visibility = View.GONE
        }
        if (success) {
            currentPhotoUri?.let { uri ->
                capturedPhotos.add(uri)
                fotoAdapter.updateData(capturedPhotos)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tugasData = arguments?.getParcelable<TugasModel>("TUGAS_DATA")
        if (tugasData == null) {
            Toast.makeText(requireContext(), "Data tugas tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        
        tugas = tugasData
        managerId = arguments?.getString("MANAGER_ID") ?: ""
        
        dbRef = FirebaseConfig.getTaskManagementRef(managerId)
            .child(tugas.villa_id)
            .child("list_tugas")
            .child(tugas.id)
            
        CloudinaryHelper.init(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUploadBuktiBinding.inflate(inflater, container, false)
        
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

        // AUTOMATIC CAMERA TRIGGER: Hanya pada entry pertama (savedInstanceState == null)
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
        binding.btnSubmit.setOnClickListener {
            submitBukti()
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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            isCameraLaunching = true
            binding.progressBar.visibility = View.VISIBLE
            
            val storageDir = requireContext().externalCacheDir
            val photoFile = File.createTempFile(
                "IMG_BUKTI_${System.currentTimeMillis()}_", 
                ".jpg", 
                storageDir
            )
            
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(), 
                "${requireContext().packageName}.fileprovider", 
                photoFile
            )
            
            cameraLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Gagal membuka kamera: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun submitBukti() {
        if (capturedPhotos.size !in 3..5) {
            Toast.makeText(requireContext(), "Ayo lengkapi fotonya! Butuh 3 sampai 5 jepretan nih.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek Internet sebelum upload yang berat
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Koneksi kamu sepertinya lagi beristirahat. Aktifkan internet untuk kirim bukti ya!", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Sedang mengunggah bukti... Tunggu sebentar ya, pahlawan villa!", Toast.LENGTH_SHORT).show()

        uploadAllPhotos { listUrlFoto ->
            val updates = HashMap<String, Any>()
            updates["status"] = FirebaseConfig.STATUS_DONE
            updates["completed_at"] = System.currentTimeMillis()
            updates["bukti_foto"] = listUrlFoto
            updates["is_validated"] = true

            dbRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Mantap! Tugas selesai dan bukti sudah terkirim.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    binding.btnSubmit.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Yah, gagal simpan ke server: ${task.exception?.message}. Coba lagi ya!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadAllPhotos(callback: (List<String>) -> Unit) {
        val uploadedUrls = mutableListOf<String>()
        var uploadCount = 0

        for (uri in capturedPhotos) {
            CloudinaryHelper.uploadImage(uri, folder = "bukti_tugas/${tugas.id}") { result ->
                if (_binding == null) return@uploadImage
                when (result) {
                    is Resource.Success -> {
                        result.data?.secure_url?.let { uploadedUrls.add(it) }
                        uploadCount++
                        if (uploadCount == capturedPhotos.size) callback(uploadedUrls)
                    }
                    is Resource.Error -> {
                        binding.btnSubmit.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Duh, proses kirim fotonya macet: ${result.message}. Cek sinyal kamu!", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
