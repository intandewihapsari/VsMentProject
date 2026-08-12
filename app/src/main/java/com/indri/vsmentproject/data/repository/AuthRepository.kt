package com.indri.vsmentproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val rootRef = FirebaseDatabase.getInstance().reference

    /**
     * Fungsi Login: Mengambil role dari root "user_mapping"
     */
    fun login(email: String, pass: String, onResult: (Resource<String>) -> Unit) {
        onResult(Resource.Loading())

        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid ?: ""

            // Mengambil data dari path: user_mapping/[uid]
            rootRef.child(FirebaseConfig.PATH_USER_MAPPING).child(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Mengambil nilai role (manager/staff)
                        val role = snapshot.child(FirebaseConfig.FIELD_ROLE).value.toString()
                        onResult(Resource.Success(role))
                    } else {
                        onResult(Resource.Error("Akses ditolak: Akun Anda tidak terdaftar dalam sistem mapping."))
                    }
                }.addOnFailureListener {
                    onResult(Resource.Error("Gagal sinkronisasi data user: ${it.message}"))
                }

        }.addOnFailureListener {
            onResult(Resource.Error("Email atau Password salah: ${it.message}"))
        }
    }

    /**
     * Fungsi Registrasi Manager: Menyimpan data ke "user_mapping" dan "villa_management"
     */
    fun registerManager(email: String, pass: String, nama: String, onResult: (Resource<Unit>) -> Unit) {
        onResult(Resource.Loading())

        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid ?: ""

            // 1. Data untuk dimasukkan ke root "user_mapping"
            val userMapping = mapOf(
                FirebaseConfig.FIELD_ROLE to "manager",
                FirebaseConfig.FIELD_BELONGS_TO_MANAGER to uid
            )

            // 2. Data profil lengkap untuk "villa_management/[uid]/manager_profile"
            val managerProfile = mapOf(
                "uid" to uid,
                "nama" to nama,
                "email" to email,
                "role" to "manager",
                "posisi" to "Supervisor",
                "status" to "aktif",
                "telepon" to "",
                "foto_profil" to ""
            )

            // PERBAIKAN: Menggunakan CHILD_MANAGER_PROFILE agar sinkron dengan FirebaseConfig
            val childUpdates = hashMapOf<String, Any>(
                "${FirebaseConfig.PATH_USER_MAPPING}/$uid" to userMapping,
                "${FirebaseConfig.PATH_VILLA_MANAGEMENT}/$uid/${FirebaseConfig.CHILD_MANAGER_PROFILE}" to managerProfile
            )

            rootRef.updateChildren(childUpdates).addOnSuccessListener {
                onResult(Resource.Success(Unit))
            }.addOnFailureListener {
                onResult(Resource.Error("Gagal menyimpan profil manager: ${it.message}"))
            }

        }.addOnFailureListener {
            onResult(Resource.Error("Gagal registrasi: ${it.message}"))
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}