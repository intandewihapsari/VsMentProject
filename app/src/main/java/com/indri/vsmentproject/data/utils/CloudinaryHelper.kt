package com.indri.vsmentproject.data.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.indri.vsmentproject.data.model.cloudinary.CloudinaryResponseModel

object CloudinaryHelper {

    // PRO-TIP: Sebaiknya detail ini dipindahkan ke BuildConfig / local.properties demi keamanan
    private const val CLOUD_NAME = "do8dnkpew"
    private const val API_KEY = "416676245931863"
    private const val API_SECRET = "-LF1d0ljWJrwLe2FdClU2IDvL3Y"

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
        try {
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Log eksepsi jika diperlukan atau biarkan jika memang sudah diinisialisasi sebelumnya
        }
    }

    fun uploadImage(
        uri: Uri,
        folder: String, // Contoh isi: "villa", "staff", "laporan"
        onResult: (Resource<CloudinaryResponseModel>) -> Unit
    ) {
        onResult(Resource.Loading())

        // Mengubah "vsment" menjadi "villa_management" agar konsisten dengan struktur database yang baru
        val targetFolder = "villa_management/$folder"

        MediaManager.get().upload(uri)
            .option("folder", targetFolder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    try {
                        // Casting bytes yang lebih aman dari Map Cloudinary
                        val bytesValue = resultData["bytes"]?.toString()?.toIntOrNull() ?: 0

                        val response = CloudinaryResponseModel(
                            public_id = resultData["public_id"].toString(),
                            secure_url = resultData["secure_url"].toString(),
                            format = resultData["format"].toString(),
                            created_at = resultData["created_at"].toString(),
                            bytes = bytesValue
                        )
                        onResult(Resource.Success(response))
                    } catch (e: Exception) {
                        onResult(Resource.Error("Gagal memproses data respon gambar: ${e.localizedMessage}"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onResult(Resource.Error(error?.description ?: "Gagal upload gambar"))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}