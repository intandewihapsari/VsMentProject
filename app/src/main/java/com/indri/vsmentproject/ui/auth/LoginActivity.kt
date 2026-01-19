package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.ActivityLoginBinding
import com.indri.vsmentproject.ui.main.ManagerActivity
import com.indri.vsmentproject.ui.main.StaffActivity
import kotlin.jvm.java


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi Email & Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
                checkRole(result.user?.uid)
            }.addOnFailureListener {
                Toast.makeText(this, "Login Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkRole(uid: String?) {
        Log.d("ROLE_CHECK", "Mencari UID: $uid")
        FirebaseDatabase.getInstance().getReference("master_data/staff")
            .child(uid!!).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val role = snapshot.child("role").value.toString()
                    Log.d("ROLE_CHECK", "Role ditemukan: $role") // Lihat di Logcat!

                    val intent = if (role == "manager") {
                        Intent(this@LoginActivity, ManagerActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, StaffActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("ROLE_CHECK", "Data UID tidak ada di database!")
                    Toast.makeText(this, "Data profil tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Log.e("ROLE_CHECK", "Gagal akses database: ${it.message}")
            }
    }
}