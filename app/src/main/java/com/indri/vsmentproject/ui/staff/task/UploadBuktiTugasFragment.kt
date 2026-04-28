package com.indri.vsmentproject.ui.staff.task

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.CloudinaryHelper
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentUploadBuktiBinding
import com.indri.vsmentproject.data.utils.Resource

class UploadBuktiTugasFragment : Fragment() {

    private var _binding: FragmentUploadBuktiBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var tugas: TugasModel

    private val selectedPhotos = mutableListOf<Uri>()

    // 🔥 PICK MULTIPLE IMAGE
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->

            if (uris.isNullOrEmpty()) return@registerForActivityResult

            if (uris.size > 5) {
                Toast.makeText(requireContext(), "Maksimal 5 foto!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            selectedPhotos.clear()
            selectedPhotos.addAll(uris)

            Toast.makeText(
                requireContext(),
                "${selectedPhotos.size} foto dipilih",
                Toast.LENGTH_SHORT
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbRef = FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)

        tugas = arguments?.getParcelable("TUGAS_DATA")!!

        // 🔥 INIT CLOUDINARY
        CloudinaryHelper.init(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBuktiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPilihFoto.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            submitBukti()
        }
    }

    // 🔥 SUBMIT BUKTI
    private fun submitBukti() {

        if (selectedPhotos.size !in 3..5) {
            Toast.makeText(requireContext(), "Harus 3–5 foto!", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_SHORT).show()

        uploadToCloudinary { listUrlFoto ->

            val updates = HashMap<String, Any>()
            updates["status"] = "selesai"
            updates["completed_at"] = System.currentTimeMillis()
            updates["bukti_foto"] = listUrlFoto
            updates["is_validated"] = true

            dbRef.child(tugas.id).updateChildren(updates)

            Toast.makeText(requireContext(), "Berhasil upload & tugas selesai!", Toast.LENGTH_SHORT).show()

            parentFragmentManager.popBackStack()
        }
    }

    // 🔥 UPLOAD KE CLOUDINARY
    private fun uploadToCloudinary(callback: (List<String>) -> Unit) {

        val uploadedUrls = mutableListOf<String>()
        var uploadCount = 0

        for (uri in selectedPhotos) {

            CloudinaryHelper.uploadImage(
                uri,
                folder = "bukti_tugas/${tugas.id}"
            ) { result ->

                when (result) {

                    is Resource.Success -> {
                        val url = result.data?.secure_url ?: ""
                        uploadedUrls.add(url)
                        uploadCount++

                        if (uploadCount == selectedPhotos.size) {
                            callback(uploadedUrls)
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            result.message ?: "Upload gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Resource.Loading -> {
                        // optional: bisa tambahin progress indicator
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}