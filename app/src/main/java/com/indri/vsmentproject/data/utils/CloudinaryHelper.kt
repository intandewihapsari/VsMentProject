package com.indri.vsmentproject.data.utils


import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.indri.vsmentproject.data.model.cloudinary.CloudinaryResponseModel

object CloudinaryHelper {

    // Konfigurasi Cloudinary (Ganti dengan detail akun Cloudinary kamu)
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
            // Sudah diinisialisasi
        }
    }


    fun uploadImage(
        uri: Uri,
        folder: String,
        onResult: (Resource<CloudinaryResponseModel>) -> Unit
    ) {
        onResult(Resource.Loading())

        MediaManager.get().upload(uri)
            .option("folder", "vsment/$folder")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    // Mapping data ke model baru
                    val response = CloudinaryResponseModel(
                        public_id = resultData["public_id"].toString(),
                        secure_url = resultData["secure_url"].toString(),
                        format = resultData["format"].toString(),
                        created_at = resultData["created_at"].toString(),
                        bytes = (resultData["bytes"] as? Int) ?: 0
                    )
                    // Sekarang tipe datanya sudah cocok!
                    onResult(Resource.Success(response))
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onResult(Resource.Error(error?.description ?: "Gagal upload gambar"))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}