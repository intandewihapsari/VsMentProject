package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.databinding.ActivityRegisterBinding
import com.indri.vsmentproject.ui.main.ManagerActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Registrasi Manager Berhasil!", Toast.LENGTH_SHORT).show()

                    // Langsung arahkan ke dashboard manager utama
                    startActivity(Intent(this, ManagerActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Gagal Daftar: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val nama = binding.etNama.text.toString().trim()

            // Validasi Input
            if (email.isEmpty() || pass.isEmpty() || nama.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter ya", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Eksekusi Registrasi via ViewModel
            viewModel.register(email, pass, nama)
        }

        binding.tvToLogin.setOnClickListener {
            finish()
        }
    }
}