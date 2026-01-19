package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ActivityLoginBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.main.StaffActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. Cek jika sudah login (Auto Login)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkRole(currentUser.uid)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tampilkan Loading
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            // 2. Proses Login Auth
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    // 3. Panggil checkRole setelah Auth Berhasil
                    checkRole(result.user?.uid)
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Login Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Fungsi Inti: Mengecek role di Realtime Database berdasarkan UID
     */

    private fun checkRole(uid: String?) {
        if (uid == null) return
        binding.progressBar.visibility = View.VISIBLE

        val rootRef = FirebaseDatabase.getInstance().reference

        // 1. Cek di folder users/managers
        rootRef.child(FirebaseConfig.PATH_MANAGERS).child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Ditemukan sebagai Manager
                startActivity(Intent(this, ManagerActivity::class.java))
                finish()
            } else {
                // 2. Jika tidak ada, cek di folder users/staffs
                rootRef.child(FirebaseConfig.PATH_STAFFS).child(uid).get().addOnSuccessListener { staffSnap ->
                    if (staffSnap.exists()) {
                        // Ditemukan sebagai Staff
                        startActivity(Intent(this, StaffActivity::class.java))
                        finish()
                    } else {
                        // Jika di kedua folder tidak ada
                        auth.signOut()
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Profil tidak ditemukan di database!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetUI() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }
}