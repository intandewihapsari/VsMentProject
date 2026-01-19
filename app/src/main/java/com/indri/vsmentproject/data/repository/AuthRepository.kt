package com.indri.vsmentproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_USERS)

    fun login(email: String, pass: String, onResult: (Resource<String>) -> Unit) {
        onResult(Resource.Loading())
        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid ?: ""
            // Ambil role dari path users/[uid] agar sinkron dengan pendaftaran
            db.child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val role = snapshot.child(FirebaseConfig.FIELD_ROLE).value.toString()
                    onResult(Resource.Success(role))
                } else {
                    onResult(Resource.Error("Data profil tidak ditemukan di database"))
                }
            }.addOnFailureListener {
                onResult(Resource.Error("Gagal mengambil data user: ${it.message}"))
            }
        }.addOnFailureListener {
            onResult(Resource.Error("Email atau Password salah: ${it.message}"))
        }
    }

    // Tambahkan fungsi ini di dalam AuthRepository.kt
    fun registerManager(email: String, pass: String, nama: String, onResult: (Resource<Unit>) -> Unit) {
        onResult(Resource.Loading())
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid ?: ""

            // Membuat objek user berdasarkan UserModel
            val user = UserModel(
                uid = uid,
                nama = nama,
                email = email,
                role = "manager",
                posisi = "Property Manager"
            )

            // Simpan ke path users/[uid]
            db.child(uid).setValue(user).addOnSuccessListener {
                onResult(Resource.Success(Unit))
            }.addOnFailureListener {
                onResult(Resource.Error("Gagal simpan profil: ${it.message}"))
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