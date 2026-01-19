package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ActivityRegisterBinding
import com.indri.vsmentproject.ui.main.ManagerActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val nama = binding.etNama.text.toString().trim()

            // 1. Validasi Input Dasar
            if (email.isEmpty() || pass.isEmpty() || nama.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi, kids!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter ya", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tampilkan Loading
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            // 2. Proses Registrasi ke Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    saveUserToDatabase(uid, nama, email)
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Gagal Daftar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvToLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    /**
     * Fungsi untuk menyimpan data profil Manager ke Realtime Database
     */
    private fun saveUserToDatabase(uid: String, nama: String, email: String) {
        val user = UserModel(
            uid = uid,
            nama = nama,
            email = email,
            role = "manager",
            posisi = "Property Manager"
        )

        // Sesuai JSON: users/managers/[uid]
        FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_MANAGERS)
            .child(uid).setValue(user).addOnSuccessListener {
                startActivity(Intent(this, ManagerActivity::class.java))
                finish()
            }
    }
}