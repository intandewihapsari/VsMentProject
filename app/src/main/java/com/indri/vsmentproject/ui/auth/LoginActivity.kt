package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.ActivityLoginBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.main.StaffActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cek Auto Login jika session Firebase Auth masih aktif
        auth.currentUser?.let {
            checkRoleDirectly(it.uid)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setButtonsEnabled(false)
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val role = resource.data ?: ""
                    navigateToDashboard(role, auth.currentUser?.uid ?: "")
                }
                is Resource.Error -> {
                    resetUI()
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Format email tidak valid"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.etPassword.error = "Password wajib diisi"
                return@setOnClickListener
            }
            
            viewModel.login(email, pass)
        }

        // Tombol Login Cepat
        binding.btnQuickStaff.setOnClickListener {
            viewModel.login("cobajustin@vsment.com", "asdfghjkl")
        }
        binding.btnQuickManager.setOnClickListener {
            viewModel.login("intan@vsment.com", "asdfghjkl")
        }

        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkRoleDirectly(uid: String) {
        binding.progressBar.visibility = View.VISIBLE
        setButtonsEnabled(false)

        // Membaca map role langsung dari root user_mapping untuk auto-login
        FirebaseDatabase.getInstance().reference.child(FirebaseConfig.PATH_USER_MAPPING).child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val role = snapshot.child(FirebaseConfig.FIELD_ROLE).value.toString()
                    navigateToDashboard(role, uid)
                } else {
                    auth.signOut()
                    resetUI()
                }
            }.addOnFailureListener {
                auth.signOut()
                resetUI()
            }
    }

    private fun navigateToDashboard(role: String, uid: String) {
        // Simpan UID user saat ini ke session local untuk filter data di Fragment/Activity lain
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPref.edit().putString("staff_id", uid).apply()

        if (role.equals("manager", ignoreCase = true)) {
            startActivity(Intent(this, ManagerActivity::class.java))
        } else {
            startActivity(Intent(this, StaffActivity::class.java))
        }
        finish()
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
                if (email.isNotEmpty()) resetPassword(email)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetPassword(email: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
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
        binding.btnQuickManager.isEnabled = enabled
    }
}