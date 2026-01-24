package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
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

        // 1. Cek Auto Login jika session Firebase masih aktif
        auth.currentUser?.let {
            checkRole(it.uid)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Tombol Login Normal
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performLogin(email, pass)
        }

        // Tombol Login Cepat (Justin Staff)
        binding.btnQuickStaff.setOnClickListener {
            binding.etEmail.setText("cobajustin@vsment.com")
            binding.etPassword.setText("asdfghjkl")
            performLogin("cobajustin@vsment.com", "asdfghjkl")
        }

        // Lupa Password
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Pindah ke Register
        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(email: String, pass: String) {
        binding.progressBar.visibility = View.VISIBLE
        setButtonsEnabled(false)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                // Login Auth sukses, lanjut cek role di Realtime Database
                checkRole(result.user?.uid)
            }
            .addOnFailureListener { e ->
                resetUI()
                Toast.makeText(this, "Login Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkRole(uid: String?) {
        if (uid == null) return
        binding.progressBar.visibility = View.VISIBLE

        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserEmail = auth.currentUser?.email

        // 1. Cari di folder managers berdasarkan email (Menggunakan Query orderByChild)
        rootRef.child(FirebaseConfig.PATH_MANAGERS)
            .orderByChild("email").equalTo(currentUserEmail)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Jika ketemu di Manager
                    saveIdToSession(snapshot)
                    startActivity(Intent(this, ManagerActivity::class.java))
                    finish()
                } else {
                    // 2. Jika tidak ada di manager, cari di staffs berdasarkan email
                    rootRef.child(FirebaseConfig.PATH_STAFFS)
                        .orderByChild("email").equalTo(currentUserEmail)
                        .get().addOnSuccessListener { staffSnap ->
                            if (staffSnap.exists()) {
                                // Jika ketemu di Staff
                                saveIdToSession(staffSnap)
                                startActivity(Intent(this, StaffActivity::class.java))
                                finish()
                            } else {
                                // Jika tidak ketemu di keduanya
                                auth.signOut()
                                resetUI()
                                Toast.makeText(this, "Profil dengan email ini tidak ditemukan di Database!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            handleError(it.message)
                        }
                }
            }.addOnFailureListener {
                handleError(it.message)
            }
    }

    // Fungsi untuk menyimpan Key (UID) ke SharedPreferences agar Fragment bisa memfilter tugas
    private fun saveIdToSession(snapshot: DataSnapshot) {
        // snapshot.children.first().key akan mengambil UID panjang (W7lU... / MIzM...)
        val id = snapshot.children.firstOrNull()?.key
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPref.edit().putString("staff_id", id).apply()
    }

    private fun showForgotPasswordDialog() {
        val inputEmail = EditText(this).apply {
            hint = "Masukkan Email Terdaftar"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(60, 20, 60, 0)
            inputEmail.layoutParams = lp
            addView(inputEmail)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(container)
            .setPositiveButton("Kirim") { _, _ ->
                val email = inputEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    resetPassword(email)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetPassword(email: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Link reset password dikirim ke email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetUI() {
        binding.progressBar.visibility = View.GONE
        setButtonsEnabled(true)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnQuickStaff.isEnabled = enabled
    }

    private fun handleError(message: String?) {
        resetUI()
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }
}