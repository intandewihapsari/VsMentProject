package com.indri.vsmentproject.data.utils


/**
 * Class wrapper untuk menangani state data dari Firebase.
 * Membantu UI (Fragment/Activity) untuk tahu kondisi Loading, Success, atau Error.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    // State saat data berhasil diambil
    class Success<T>(data: T) : Resource<T>(data)

    // State saat terjadi error (misal: internet mati atau permission denied)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    // State saat aplikasi sedang menunggu respon dari Firebase
    class Loading<T>(data: T? = null) : Resource<T>(data)
}