package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    // Fungsi Login
    fun login(email: String, pass: String, onResult: (Boolean, String?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid
            // Setelah login sukses, langsung ambil role dari database
            db.child(uid!!).get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").value.toString()
                onResult(true, role, null)
            }
        }.addOnFailureListener {
            onResult(false, null, it.message)
        }
    }

    // Cek apakah user masih login (Auto-Login)
    fun getCurrentUserRole(onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.child(uid).child("role").get().addOnSuccessListener {
                onResult(it.value.toString())
            }.addOnFailureListener { onResult(null) }
        } else {
            onResult(null)
        }
    }

    fun logout() {
        auth.signOut()
    }
}