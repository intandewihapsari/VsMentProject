package com.indri.vsmentproject.data.utils


/**
 * Class wrapper untuk menangani state data dari Firebase.
 * Membantu UI (Fragment/Activity) untuk tahu kondisi Loading, Success, atau Error.
 */
sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null
) {
    // State saat data berhasil diambil
    class Success<out T>(data: T) : Resource<T>(data)

    // State saat terjadi error
    class Error<out T>(message: String, data: T? = null) : Resource<T>(data, message)

    // Menggunakan konstruktor tanpa argumen sebagai fallback agar pemanggilan lebih bersih
    class Loading<out T>(data: T? = null) : Resource<T>(data)
}